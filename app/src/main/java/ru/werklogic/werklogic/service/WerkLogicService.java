package ru.werklogic.werklogic.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;
import ru.werklogic.werklogic.commands.BaseCommand;
import ru.werklogic.werklogic.commands.ConnectNewClientCommand;
import ru.werklogic.werklogic.commands.UpdateConfigCommand;
import ru.werklogic.werklogic.dm.DataModel;
import ru.werklogic.werklogic.marshalling.Packer;
import ru.werklogic.werklogic.protocol.channel.UsbChannel;
import ru.werklogic.werklogic.protocol.data.HardwareSensorInfo;
import ru.werklogic.werklogic.protocol.data.IResponseData;
import ru.werklogic.werklogic.protocol.facade.IDataListener;
import ru.werklogic.werklogic.protocol.facade.Rec31Facade;
import ru.werklogic.werklogic.utils.Utils;

import static android.util.Log.i;
import static ru.werklogic.werklogic.utils.Utils.log;

public class WerkLogicService extends Service implements IWerkLogicService {

    private static final String WS_BASE_URL = "ws://93.95.196.246:8080/0/";

    private UsbChannel channel;
    private WerkLogicServiceBinder binder = new WerkLogicServiceBinder();
    private Rec31Facade rec31Facade = new Rec31Facade();
    private Thread setupSensorThread;
    private BroadcastReceiver broadcastReceiver;
    private android.content.IntentFilter broadcastFilter;
    private DataModel dm;
    private WebSocketConnection mConnection;
    private String cloudIdForWebSocketUrl;

    @Override
    public boolean connectUsbDevice(UsbDevice usbDevice) {
        initChannel();
        if (channel.connect(usbDevice)) {
            rec31Facade.init(channel, new IDataListener() {
                @Override
                public void onData(IResponseData data) {
                    Utils.log("Из устройства приняты данные общим слушателем: " + data.toString());
                    if (dm.isSpyMode()) {
                        if (data instanceof HardwareSensorInfo) {
                            Utils.log("Данные распознаны");
                            HardwareSensorInfo hwi = (HardwareSensorInfo) data;
                            dm.saveEvent(hwi, new Date());
                        } else {
                            Utils.log("Данные не распознаны");
                        }
                    }
                }

                @Override
                public void onConnect() {

                }

                @Override
                public void onDisconnect() {

                }
            });
            return true;
        }
        return false;
    }

    @Override
    public void disconnectUsbDevice() {
        closeChannel();
    }

    @Override
    public String readChecksum() {
        return rec31Facade.readChecksum();
    }

    @Override
    public void setupSensor(final byte sensorNumber, final Rec31Facade.ISetupDetectorListener iSetupDetectorListener) {
        if (setupSensorThread != null)
            setupSensorThread.interrupt();
        setupSensorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    rec31Facade.setupSensor(sensorNumber, new Rec31Facade.ISetupDetectorListener() {

                        @Override
                        public void onFirstClick() {
                            iSetupDetectorListener.onFirstClick();
                        }

                        @Override
                        public void onSecondClick() {
                            iSetupDetectorListener.onSecondClick();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            iSetupDetectorListener.onFailure(e);
                        }

                        @Override
                        public void onSuccess(HardwareSensorInfo sensorInfo) {
                            iSetupDetectorListener.onSuccess(sensorInfo);
                        }
                    });
                } catch (Exception e) {
                    iSetupDetectorListener.onFailure(e);
                }

            }
        });
        setupSensorThread.start();
    }

    @Override
    public boolean deleteSensor(byte sensorNumber) {
        return rec31Facade.deleteSensor(sensorNumber);
    }

    public class WerkLogicServiceBinder extends Binder {
        public IWerkLogicService getService() {
            return WerkLogicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.log(getClass().getSimpleName() + ": create service");
        dm = Utils.getApplication(this).getDataModel();

        initWSThread();

        broadcastReceiver = new LocalBroadcastReceiver();
        broadcastFilter = new IntentFilter();
        broadcastFilter.addAction(DataModel.DETACH_ACTION);
        broadcastFilter.addAction(DataModel.DATA_REFRESH_ACTION);
        registerReceiver(broadcastReceiver, broadcastFilter);
        Utils.log("Зарегистрирован слушатель для action " + DataModel.DETACH_ACTION);
    }

    private Timer connectionCheckTimer;
    private boolean connecting;

    private void checkConnection() {
        if (mConnection == null)
            mConnection = new WebSocketConnection();
        if (!mConnection.isConnected() && !connecting) {
            connecting = true;
            try {
                cloudIdForWebSocketUrl = dm.getCloudId();
                final String wsUri = WS_BASE_URL + cloudIdForWebSocketUrl;
                Utils.log("Попытка установить соединение по URL=" + wsUri);
                mConnection.connect(wsUri, new WebSocketConnectionHandler() {
                    @Override
                    public void onOpen() {
                        connecting = false;
                        Utils.log("Установлено соединение через web-сокет");
                        dm.setWsConnectionStatus(true);
                        if (dm.isConfigInternal())
                            processCommand(new UpdateConfigCommand());
                        else
                            processCommand(new ConnectNewClientCommand());
                    }

                    @Override
                    public void onTextMessage(String payload) {
                        //Utils.log("Получено сообщение: " + payload);
                        executeReceivedData(payload);
                    }

                    @Override
                    public void onClose(int code, String reason) {
                        Utils.log("Соединение по web-сокету потеряно");
                        connecting = false;
                        dm.setWsConnectionStatus(false);
                    }

                });
            } catch (WebSocketException e) {
//                Utils.log("Ошибка сокета: " + Utils.getStackTrace(e));
            }
        }
    }

    private void initWSThread() {
        final Handler uiHandler = new Handler();
        connectionCheckTimer = new Timer();
        connectionCheckTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        checkConnection();
                    }
                });
            }
        }, 0L, 5000);

    }

    @Override
    public void processCommand(BaseCommand localCommand) {
        Utils.log("Получена команда на исполнение: " + localCommand);
        BaseCommand externalCommand = null;
        if (localCommand != null) {
            localCommand.setEnvironment(dm, this);
            if (dm.isConfigInternal()) {
                Utils.log("Исполнение метода executeLocalOnInternalConfig у команды");
                externalCommand = localCommand.executeLocalOnInternalConfig();
            } else {
                Utils.log("Исполнение метода executeLocalOnExternalConfig у команды");
                externalCommand = localCommand.executeLocalOnExternalConfig();
            }
        }
        if (externalCommand != null) {
            Utils.log("Сформирована исходящая команда: " + externalCommand);
            if (dm.isConfigInternal()) {
                externalCommand.setEnvironment(dm, this);
                Utils.log("Исполнение метода executeOnInternalConfig у команды");
                externalCommand.executeOnInternalConfig();
            }
            if (mConnection != null && mConnection.isConnected()) {
                Utils.log("Посылка команды в облако");
                sendCommand(externalCommand);
            }
        }
    }

    private void sendCommand(BaseCommand command) {
        command.setEnvironment(dm, this);
        byte[] data = Packer.marshall(command);
        try {
            String text = Base64.encodeToString(data, Base64.NO_WRAP);
            Utils.log("Отправлено сообщение через веб-сокет: " + command);//+ ", " + text);

            byte[] d = Base64.decode(text, Base64.NO_WRAP);
            BaseCommand c = Packer.unmarshall(d);
            //Utils.log("Обратное преобразование в команду: " + c);

            mConnection.sendTextMessage(text);
        } catch (Throwable e) {
            Utils.log("Ошибка отправки сообщения: " + Utils.getStackTrace(e));
        }
    }

    private void executeReceivedData(String s) {
        byte[] data = Base64.decode(s, Base64.NO_WRAP);
        BaseCommand command = null;
        try {
            command = Packer.unmarshall(data);
            Utils.log("Исполнение команды: " + command);
        } catch (Throwable e) {
            Utils.log("Ошибка преобразования потока байтов в команду: " + Utils.getStackTrace(e));
        }
        if (command != null) {
            try {
                command.setEnvironment(dm, this);
                if (dm.isConfigInternal()) {
                    command.executeOnInternalConfig();
                } else {
                    command.executeOnExternalConfig();
                }
            } catch (Throwable e) {
                Utils.log("Ошибка исполнения команды " + command.toString() + ": " + Utils.getStackTrace(e));
            }
        }
    }

    private void initChannel() {
        // TODO на всякий случай
        closeChannel();

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        channel = new UsbChannel(manager);
        channel.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Utils.log(getClass().getSimpleName() + ": onStartCommand");
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        Utils.log("Дерегистрирован слушатель для action " + DataModel.DETACH_ACTION);

        if (mConnection.isConnected()) {
            mConnection.disconnect();
        }
        connectionCheckTimer.cancel();

        closeChannel();

        Utils.log(getClass().getSimpleName() + ": destroy service");
        super.onDestroy();
    }

    private void closeChannel() {
        if (channel != null) {
            channel.disconnect();
            channel.interrupt();
            channel = null;
        }
    }

    private class LocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (DataModel.DETACH_ACTION.equals(intent.getAction())) {
                log("Получено событие отсоединения USB в " + WerkLogicService.class.getSimpleName());
                disconnectUsbDevice();
                dm.unloadSensors();
                log("Таблица датчиков очищена");
            } else if (DataModel.DATA_REFRESH_ACTION.equals(intent.getAction())) {
                if (cloudIdForWebSocketUrl != null && !cloudIdForWebSocketUrl.equals(dm.getCloudId())) {
                    mConnection.disconnect();
                    checkConnection();
                }
                if (dm.isConfigInternal()) {
                    if (intent.getBooleanExtra(DataModel.SAVE_CONFIG_COMMAND_PARAM, false)) {
                        dm.saveConfig();
                    }
                    if (intent.getBooleanExtra(DataModel.SEND_EXTERNAL_REFRESH_COMMAND_PARAM, false)) {
                        processCommand(new UpdateConfigCommand());
                    }
                }
            }
        }

    }
}




