package ru.werklogic.werklogic.dm;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.werklogic.werklogic.dm.events.EventType;
import ru.werklogic.werklogic.protocol.data.HardwareSensorInfo;
import ru.werklogic.werklogic.utils.Utils;

/**
 * Created by bmw on 10.06.2015.
 */
public class Config implements Serializable, Parcelable {
    private List<SensorState> sensorsStates = new ArrayList<>();
    private List<SmsItem> smsItems = new ArrayList<>();
    private String checkSum;
    /* идентификатор облака. Облако привязано к "хвосту" */
    private String cloudId;
    private boolean isSpyMode;
    private boolean isUsbAttached;

    public Config() {
    }

    public boolean isUsbAttached() {
        return isUsbAttached;
    }

    public void setUsbAttached(boolean isUsbAttached) {
        this.isUsbAttached = isUsbAttached;
    }

    public boolean isSpyMode() {
        return isSpyMode;
    }

    public synchronized void setSpyMode(boolean isSpyMode) {
        this.isSpyMode = isSpyMode;
        if (!isSpyMode) {
            for (SensorState s : sensorsStates) {
                s.saveEvent(new Date(), EventType.READ);
            }
        }
    }

    public String getCloudId() {
        if (cloudId == null) {
// TODO заменить на setCloudId(Utils.genGuid());
//            setCloudId("happy-gda");
            setCloudId("123");
        }
        return cloudId;
    }

    private void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }

    public List<SmsItem> getSmsItems() {
        return smsItems;
    }

    public String getCheckSum() {
        if (checkSum == null)
            checkSum = Utils.genCheckSum();
        return checkSum;
    }

    /**
     * Загрузка конфигурации из файла.
     *
     * @param checkSum
     */
    public boolean loadInternalConfig(String checkSum) {
        Config config = null;
        boolean result = false;
        if (checkSum != null) {
            Utils.log("Чтение конфигурации по checkSum=" + checkSum);
            String appDir = Utils.getConfigsDir();
            String fileName = Utils.getConfigFileName(checkSum);
            File configFile = new File(appDir, fileName);
            if (configFile.exists()) {
                ObjectInputStream is = null;
                try {
                    is = new ObjectInputStream(new FileInputStream(configFile));
                    config = (Config) is.readObject();
                    Utils.log("Конфигурация загружена успешно. Число датчиков = " + sensorsStates.size() +
                            ", число номеров для СМС = " + smsItems.size());
                    result = true;
                } catch (Exception e) {
                    Utils.log("Ошибка загрузки конфигурации из файла " + fileName + ": " + Utils.getStackTrace(e));
                } finally {
                    if (is != null)
                        try {
                            is.close();
                        } catch (IOException e) {
                            Utils.log("Ошибка закрытия файла " + fileName);
                        }
                }
            } else {
                Utils.log("Ранее сохраненная конфигурация не обнаружена. Используется пустая");
            }
        } else {
            Utils.log("Установлена пустая конфигурация для неустановленной контрольной суммы");
        }
        setConfig(config);
        return result;
    }

    public List<SensorState> getSensorsStates() {
        return sensorsStates;
    }

    public void unloadConfig() {
        checkSum = null;
        sensorsStates.clear();
        smsItems.clear();
        cloudId = null;
        isSpyMode = false;
    }

    public void addHardwareSensorInfo(HardwareSensorInfo s) {
        SensorState state = new SensorState(s);
        sensorsStates.add(state);
    }

    // TODO после вызова записать контрольную сумму в устройство
    public void saveConfig() {

        checkSum = Utils.genCheckSum();

        String appDir = Utils.getConfigsDir();
        String fileName = Utils.getConfigFileName(checkSum);
        File configFile = new File(appDir, fileName);
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(new FileOutputStream(configFile));
            os.writeObject(this);
            Utils.log("Конфигурация успешно записана");
        } catch (Exception e) {
            Utils.log("Ошибка записи конфигурации в файл " + fileName + ": " + Utils.getStackTrace(e));
        } finally {
            if (os != null)
                try {
                    os.close();
                } catch (IOException e) {
                    Utils.log("Ошибка закрытия файла " + fileName);
                }
        }
    }

    public void setConfig(Config config) {
        isSpyMode = false;
        cloudId = null;
        checkSum = null;
        sensorsStates.clear();
        smsItems.clear();
        if (config != null) {
            isSpyMode = config.isSpyMode;
            cloudId = config.cloudId;
            checkSum = config.checkSum;
            if (config.getSensorsStates() != null) {
                for (SensorState s : config.getSensorsStates()) {
                    if (s != null) {
                        sensorsStates.add(s);
                        s.getRecentEvents();
                    }
                }
            }
            if (config.getSmsItems() != null) {
                for (SmsItem s : config.getSmsItems()) {
                    if (s != null)
                        smsItems.add(s);
                }
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.sensorsStates);
        dest.writeList(this.smsItems);
        dest.writeString(this.checkSum);
        dest.writeString(this.cloudId);
        dest.writeByte(isSpyMode ? (byte) 1 : (byte) 0);
        dest.writeByte(isUsbAttached ? (byte) 1 : (byte) 0);
    }

    private Config(Parcel in) {
        this.sensorsStates = new ArrayList<>();
        in.readList(this.sensorsStates, getClass().getClassLoader());
        this.smsItems = new ArrayList<>();
        in.readList(this.smsItems, getClass().getClassLoader());
        this.checkSum = in.readString();
        this.cloudId = in.readString();
        this.isSpyMode = in.readByte() != 0;
        this.isUsbAttached = in.readByte() != 0;
    }

    public static final Creator<Config> CREATOR = new Creator<Config>() {
        public Config createFromParcel(Parcel source) {
            return new Config(source);
        }

        public Config[] newArray(int size) {
            return new Config[size];
        }
    };

    @Override
    public String toString() {
        return "Config{" +
                "sensorsStates=" + sensorsStates +
                ", smsItems=" + smsItems +
                ", checkSum='" + checkSum + '\'' +
                ", cloudId='" + cloudId + '\'' +
                ", isSpyMode=" + isSpyMode +
                ", isUsbAttached=" + isUsbAttached +
                '}';
    }
}
