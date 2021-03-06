package ru.werklogic.werklogic.dm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ru.werklogic.werklogic.R;
import ru.werklogic.werklogic.dm.events.EventType;
import ru.werklogic.werklogic.protocol.data.HardwareSensorInfo;
import ru.werklogic.werklogic.utils.Utils;

/**
 * Created by bmw on 12.05.2015.
 */
public class DataModel {

    public static final String DATA_REFRESH_ACTION = "ru.werklogic.werklogic.DATA_REFRESH_ACTION";
    public static final String DETACH_ACTION = "ru.werklogic.werklogic.DETACH_ACTION";
    public static final String SEND_EXTERNAL_REFRESH_COMMAND_PARAM = "isConfigChanged";
    public static final String SAVE_CONFIG_COMMAND_PARAM = "saveConfig";
    private static final String PREF_KEY_LAST_CHECKSUM = "last_checksum";

    private static final String PREF_KEY_PASSWORD = "password";
    private final Context context;

    private Config config = new Config();

    public Config getConfig() {
        return config;
    }

    public void setConfigInternal(boolean isConfigInternal) {
        this.isConfigInternal = isConfigInternal;
    }

    private boolean isConfigInternal = false;
    private boolean wsConnectionStatus;

    public synchronized boolean isAlert() {
        boolean result = false;
        for (SensorState s : getSensorsStates()) {
            result |= s.isAlert();
        }
        return result;
    }

    DataModel(Context context) {
        this.context = context;
    }

    public boolean isConfigInternal() {
        return isConfigInternal;
    }

    private void sendDataRefreshEvent(boolean isSendExternalRefreshCommand, boolean saveConfigFlag) {
        Intent intent = new Intent(DataModel.DATA_REFRESH_ACTION);
        intent.putExtra(SAVE_CONFIG_COMMAND_PARAM, saveConfigFlag);
        intent.putExtra(SEND_EXTERNAL_REFRESH_COMMAND_PARAM, isSendExternalRefreshCommand);
        context.sendBroadcast(intent);
    }

    public synchronized SensorState getSensorByHardwareSensorInfo(HardwareSensorInfo hardwareSensorInfo) {
        if (hardwareSensorInfo != null)
            for (SensorState ss : config.getSensorsStates()) {
                if (hardwareSensorInfo.getSensorNumber() == ss.getHardwareSensorInfo().getSensorNumber())
                    return ss;
            }
        return null;
    }

    public synchronized void setUsbAttached(boolean isUsbAttached) {
        config.setUsbAttached(isUsbAttached);
    }

    public synchronized void saveConfig() {
        Utils.log("Запись конфигурации");
        String oldCheckSum = config.getCheckSum();
        Utils.log("checkSum до сохранения конфига: " + oldCheckSum);
        config.saveConfig();
        String newCheckSum = config.getCheckSum();
        Utils.log("checkSum после сохранения конфига: " + newCheckSum);
        setLastCheckSum(newCheckSum);
        if (!newCheckSum.equals(oldCheckSum)) {
            Utils.log("Удаление файла конфигурации для checkSum=" + oldCheckSum);
            deleteConfigFile(oldCheckSum);
        }
    }

    private void deleteConfigFile(String checkSum) {
        String appDir = Utils.getConfigsDir();
        String fileName = Utils.getConfigFileName(checkSum);
        File configFile = new File(appDir, fileName);
        configFile.delete();
    }

    public synchronized void setSensorName(HardwareSensorInfo s, String name) {
        SensorState ss = getSensorByHardwareSensorInfo(s);
        if (ss != null) {
            ss.setName(name);
            sendDataRefreshEvent(true, true);
        }
    }

    public synchronized void saveEvent(SensorState ss, Date d) {
//        Utils.log("Сохранение события в файле. SensorId=" + ss.getGuid() + ", date=" + d + ", ALERT");
        ss.saveEvent(d, EventType.ALERT);
        sendDataRefreshEvent(true, false);
    }

    public synchronized SensorState preprocessEvent(HardwareSensorInfo s) {
        SensorState ss = getSensorByHardwareSensorInfo(s);
        if (ss == null) {
            Utils.log("Не найден датчик, которому принадлежат данные");
            return null;
        }
        if (!ss.isActive()) {
            Utils.log("Датчик не активен");
            return null;
        }
        return ss;
    }

    public synchronized List<SensorState> getSensorsStates() {
        return config.getSensorsStates();
    }

    public synchronized void setCloudId(String cloudId) {
        config.setCloudId(cloudId);
        sendDataRefreshEvent(false, true);
    }

    public synchronized String getSensorsCheckSum() {
        return config.getCheckSum();
    }

    public synchronized byte getFreeSensorAddress() {
        boolean b[] = new boolean[256];
        for (SensorState ss : config.getSensorsStates()) {
            b[ss.getHardwareSensorInfo().getSensorNumber() & 0xFF] = true;
        }
        for (int i = 0; i < 256; i++)
            if (!b[i])
                return (byte) i;
        return -1;
    }

    /**
     * Установка конфигурации, прочитанной из облака.
     *
     * @param config
     */
    public synchronized void setExternalConfig(Config config) {
        isConfigInternal = false;
        this.config.setConfig(config);
        sendDataRefreshEvent(false, false);
    }

    public synchronized boolean loadInternalConfigByCheckSum(String checkSum) {
        isConfigInternal = true;
        boolean result = config.loadInternalConfig(checkSum);
        String lastCheckSum = getLastCheckSum();
        setLastCheckSum(checkSum);
        if (lastCheckSum == null || !lastCheckSum.equals(checkSum)) {
            setSpyMode(false);
        }
        setUsbAttached(true);
        sendDataRefreshEvent(true, false);
        return result;
    }

    public String getLastCheckSum() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String checkSum = prefs.getString(PREF_KEY_LAST_CHECKSUM, null);
        return checkSum;
    }

    private void setLastCheckSum(String checkSum) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_KEY_LAST_CHECKSUM, checkSum);
        editor.apply();
    }

    public synchronized void unloadSensors() {
        setUsbAttached(false);
        config.unloadConfig();
        sendDataRefreshEvent(true, false);
    }

    public synchronized void setActive(HardwareSensorInfo s, boolean isActive) {
        SensorState ss = getSensorByHardwareSensorInfo(s);
        if (ss != null) {
            ss.setActive(isActive);
            sendDataRefreshEvent(true, true);
        }
    }

    public boolean needAuth() {
        String password = getPassword();
        return password != null && password.length() > 0;
    }

    public String getPassword() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String password = prefs.getString(PREF_KEY_PASSWORD, null);
        return password;
    }

    public void setPassword(String password) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_KEY_PASSWORD, password);
        editor.apply();
        sendDataRefreshEvent(false, false);
    }

    public List<SmsItem> getSmsList() {
        return config.getSmsItems();
    }

    public synchronized void setSmsNumber(SmsItem sms, String number) {
        List<SmsItem> list = config.getSmsItems();
        if (list != null && sms != null && sms.getNumber() != null && number != null) {
            boolean changed = false;
            for (SmsItem s : list)
                if (sms.getNumber().equals(s.getNumber())) {
                    s.setNumber(number);
                    changed = true;
                }
            if (changed)
                sendDataRefreshEvent(true, true);
        }
    }

    public synchronized void addSms(SmsItem sms) {
        config.getSmsItems().add(sms);
        sendDataRefreshEvent(true, true);
    }

    public void deleteSms(SmsItem sms) {
        config.getSmsItems().remove(sms);
        sendDataRefreshEvent(true, true);
    }

    public void updateSmsActive(SmsItem sms, boolean active) {
        sms.setActive(active);
        sendDataRefreshEvent(true, true);
    }

    public synchronized void setSensorState(HardwareSensorInfo s, SensorState.State state) {
        SensorState ss = getSensorByHardwareSensorInfo(s);
        if (ss != null) {
            ss.setState(state);
            if (SensorState.State.STEP1.equals(state) || SensorState.State.STEP2.equals(state))
                sendDataRefreshEvent(false, false);
            else
                sendDataRefreshEvent(true, true);
        }

    }

    public synchronized void setSensorActions(HardwareSensorInfo s, int sensorTypeNumber, int action1, int action2, int action3, int action4) {
        SensorState ss = getSensorByHardwareSensorInfo(s);
        if (ss != null) {
            ss.setSensorType(sensorTypeNumber, action1, action2, action3, action4);
            sendDataRefreshEvent(true, true);
        }
    }

    public synchronized void setSensorInitedState(HardwareSensorInfo hwi, int button, boolean batteryHigh) {
        SensorState ss = getSensorByHardwareSensorInfo(hwi);
        if (ss != null) {
            ss.setState(SensorState.State.INIT_ACTIVE);
            hwi.setButton(button);
            hwi.setBatteryHigh(batteryHigh);
            sendDataRefreshEvent(true, true);
        }
    }


    public synchronized void resetSensorState(HardwareSensorInfo hwi) {
        SensorState ss = getSensorByHardwareSensorInfo(hwi);
        if (ss != null) {
            ss.setState(SensorState.State.NOT_INIT);
            sendDataRefreshEvent(true, true);
        }
    }

    public synchronized void removeSensor(HardwareSensorInfo hwi) {
        SensorState ss = getSensorByHardwareSensorInfo(hwi);
        if (ss != null) {
            config.getSensorsStates().remove(ss);
            sendDataRefreshEvent(true, true);
        }
    }

    /**
     * Добавление нового сенсора и установка у него статуса IN_INIT_PROCESS.
     *
     * @param s хардварный сенсор
     */
    public synchronized void addSensor(HardwareSensorInfo s) {
        config.addHardwareSensorInfo(s);
        final SensorState state = getSensorByHardwareSensorInfo(s);
        if (state != null) {
            state.setState(SensorState.State.IN_INIT_PROCESS);
        }
        sendDataRefreshEvent(true, true);
    }

    public void setWsConnectionStatus(boolean wsConnectionStatus) {
        this.wsConnectionStatus = wsConnectionStatus;
        sendDataRefreshEvent(false, false);
    }

    public boolean isWSConnectionStatus() {
        return wsConnectionStatus;
    }

    public synchronized void setSpyMode(boolean isSpyMode) {
        stopSchedulingSpyMode();
        config.setSpyMode(isSpyMode);
        sendDataRefreshEvent(true, true);
    }

    private Timer scheduleSpyModeTimer;
    private Timer scheduleBeeperTimer;

    private MediaPlayer spyPlayer;

    public synchronized void stopSchedulingSpyMode() {
        if (scheduleSpyModeTimer != null) {
            scheduleSpyModeTimer.cancel();
            scheduleSpyModeTimer = null;
        }

        if (scheduleBeeperTimer != null) {
            scheduleBeeperTimer.cancel();
            scheduleBeeperTimer = null;
        }
    }

    public synchronized void scheduleSpyMode() {
        stopSchedulingSpyMode();
        scheduleBeeperTimer = new Timer();
        scheduleBeeperTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //beep
                if (spyPlayer == null) {
                    spyPlayer = MediaPlayer.create(context, R.raw.spy1_cut);
                    try {
                        spyPlayer.prepare();
                    } catch (IllegalStateException | IOException e) {
                        Utils.log("Исключение при выполнении spyPlayer.prepare(): " + Utils.getStackTrace(e));
                    }
//                    spyPlayer.setVolume(1.0f, 1.0f);
                    spyPlayer.setVolume(0.1f, 1.0f);
                }
                if (!spyPlayer.isPlaying())
                    spyPlayer.start();
            }
        }, 0, 500);
        scheduleSpyModeTimer = new Timer();
        scheduleSpyModeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                setSpyMode(true);
            }
        }, 30000);
    }

    public boolean isSpyMode() {
        return config.isSpyMode();
    }

    public String getCloudId() {
        return config.getCloudId();
    }

    public void setSensorType(HardwareSensorInfo s, int sensorTypeNumber, int action1, int action2, int action3, int action4) {
        SensorState ss = getSensorByHardwareSensorInfo(s);
        if (ss != null) {
            ss.setSensorType(sensorTypeNumber, action1, action2, action3, action4);
            sendDataRefreshEvent(true, true);
        }

    }

    public void sendSms(SensorState s, Date date) {
//            String phoneNumber = "0123456789";
//            String message = "Hello World! Now we are going to demonstrate " +
//                    "how to send a message with more than 160 characters from your Android application.";
//
//            SmsManager smsManager = SmsManager.getDefault();
//            ArrayList<String> parts = smsManager.divideMessage(message);
//            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
    }
}
