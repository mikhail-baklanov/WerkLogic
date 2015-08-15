package ru.werklogic.werklogic.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;

import ru.werklogic.werklogic.R;
import ru.werklogic.werklogic.dm.DataModel;
import ru.werklogic.werklogic.service.AutoUnbindServiceConnection;
import ru.werklogic.werklogic.service.IWerkLogicService;
import ru.werklogic.werklogic.service.RunnableWithParameter;
import ru.werklogic.werklogic.service.WerkLogicService;
import ru.werklogic.werklogic.utils.Utils;

import static ru.werklogic.werklogic.utils.Utils.log;

public class DeviceAttachActivity extends Activity {

    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    private UsbManager manager;
    private UsbServiceReceiver usbServiceReceiver;
    private AsyncTask<Void, Void, Void> setUsbDeviceTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("Вызов " + DeviceAttachActivity.class.getSimpleName() + ".onCreate()");
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        boolean needFinish = true;

        DataModel dm = Utils.getApplication(DeviceAttachActivity.this).getDataModel();
        // проверяем, что девайс еще не подключен в облаке
        if (!dm.getConfig().isUsbAttached()) {
            if (manager != null) {
                UsbDevice device = getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    registerReceiver();
//                log("Подключенное устройство найдено. Запрос привилегий");
                    PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    manager.requestPermission(device, permissionIntent);
                    needFinish = false;
                } else {
                    log("getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE) вернул null");
                }
            } else {
                log("getSystemService(Context.USB_SERVICE) вернул null");
            }
        }
        if (needFinish)
            finish();
        else
            setContentView(R.layout.activity_usb_on);
    }

    private void registerReceiver() {
        usbServiceReceiver = new UsbServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_USB_PERMISSION);
        registerReceiver(usbServiceReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        if (usbServiceReceiver != null) {
            unregisterReceiver(usbServiceReceiver);
            usbServiceReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }


    private class UsbServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//            log("Вызван DeviceAttachActivity.UsbServiceReceiver.onReceive(), intent=" + intent);
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device != null) {
//                        log("Даны привилегии на доступ к " + device);
                        if (setUsbDeviceTask != null)
                            setUsbDeviceTask.cancel(true);
                        setUsbDeviceTask = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                Utils.startService(DeviceAttachActivity.this);
                                connectUsbDevice(device);
                                return null;
                            }
                        };
                        setUsbDeviceTask.execute();
                    } else {
                        log("Привилегии на доступ к устройству даны, но device = null");
                    }
                } else {
                    log("Доступ к устройству " + device + " запрещен");
                }
            }
            DeviceAttachActivity.this.finish();
        }
    }

    public void connectUsbDevice(final UsbDevice usbDevice) {

        AutoUnbindServiceConnection connection = new AutoUnbindServiceConnection(this, new RunnableWithParameter<IWerkLogicService>() {
            @Override
            public void run(IWerkLogicService service) {
                if (service.connectUsbDevice(usbDevice)) {
                    DataModel dm = Utils.getApplication(DeviceAttachActivity.this).getDataModel();
                    String checkSum = service.readChecksum();
                    if (checkSum == null || checkSum.trim().length() == 0) {
                        // TODO изменить после того, как будет сохраняться контрольная сумма на устройстве
                        checkSum = dm.getLastCheckSum(); //Utils.genCheckSum();
                    } else Utils.log("Контрольная сумма получена. Сумма = " + checkSum + ".");
                    dm.loadInternalConfigByCheckSum(checkSum);

                    Intent intent = new Intent(getApplicationContext(),
                            MainActivity.class);
                    startActivity(intent);
                }
            }
        });
        bindService(new Intent(this, WerkLogicService.class), connection, 0);
    }

}
