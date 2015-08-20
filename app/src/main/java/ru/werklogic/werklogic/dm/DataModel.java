package ru.werklogic.werklogic.dm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.Date;
import java.util.List;

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
    private final Context c;

    private Config config = new Config();

    public Config getConfig() {
        return config;
    }

    private boolean isConfigInternal = false;
    private boolean wsConnectionStatus;

    DataModel(Context c) {
        this.c = c;
    }

    public boolean isConfigInternal() {
        return isConfigInternal;
    }

    private void sendDataRefreshEvent(boolean isSendExternalRefreshCommand, boolean saveConfigFlag) {
        Intent intent = new Intent(DataModel.DATA_REFRESH_ACTION);
        intent.putExtra(SAVE_CONFIG_COMMAND_PARAM, saveConfigFlag);
        intent.putExtra(SEND_EXTERNAL_REFRESH_COMMAND_PARAM, isSendExternalRefreshCommand);
        c.sendBroadcast(intent);
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

    public synchronized void saveEvent(HardwareSensorInfo s, Date d) {
        SensorState ss = getSensorByHardwareSensorInfo(s);
        if (ss == null) {
            Utils.log("Не найден датчик, которому принадлежат данные");
            return;
        }
        if (!ss.isActive()) {
            Utils.log("Датчик не активен");
            return;
        }
//        Utils.log("Сохранение события в файле. SensorId=" + ss.getGuid() + ", date=" + d + ", ALERT");
        ss.saveEvent(d, EventType.ALERT);
        sendDataRefreshEvent(true, false);
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String checkSum = prefs.getString(PREF_KEY_LAST_CHECKSUM, null);
        return checkSum;
    }

    private void setLastCheckSum(String checkSum) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(c);
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String password = prefs.getString(PREF_KEY_PASSWORD, null);
        return password;
    }

    public void setPassword(String password) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(c);
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
        config.setSpyMode(isSpyMode);
        sendDataRefreshEvent(true, true);
    }

    public boolean isSpyMode() {
        return config.isSpyMode();
    }

    public String getCloudId() {
        return config.getCloudId();
    }
}
