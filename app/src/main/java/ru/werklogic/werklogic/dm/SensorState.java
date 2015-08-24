package ru.werklogic.werklogic.dm;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.werklogic.werklogic.BuildConfig;
import ru.werklogic.werklogic.R;
import ru.werklogic.werklogic.dm.events.EventStorage;
import ru.werklogic.werklogic.dm.events.EventType;
import ru.werklogic.werklogic.dm.events.SingleSensorEventsReader;
import ru.werklogic.werklogic.protocol.data.HardwareSensorInfo;
import ru.werklogic.werklogic.utils.Utils;

/**
 * Created by bmw on 16.05.2015.
 */
public class SensorState implements Parcelable, Serializable {

    public static final Parcelable.Creator<SensorState> CREATOR = new Parcelable.Creator<SensorState>() {
        public SensorState createFromParcel(Parcel source) {
            return new SensorState(source);
        }

        public SensorState[] newArray(int size) {
            return new SensorState[size];
        }
    };
    private static final int MAX_RECENT_EVENTS_COUNT = 4;
    private String name;

    private HardwareSensorInfo hardwareSensorInfo; // 8+2+1 = 11 бит
    private boolean isAlert; // 1 бит
    private State state; // 3 бита
    private int sensorTypeNumber; // 2 бита
    // 4x3 = 12 бит
    private ActionType b1Action; // действие по нажатию первой кнопки / срабатыванию по первому каналу
    private ActionType b2Action; // действие по нажатию второй кнопки / срабатыванию по второму каналу
    private ActionType b3Action; // ...третьей...
    private ActionType b4Action; // ...червертой...
    // Итого: 11+1+3+2+12 = 29 бит

    private String guid;
    private List<Date> recentEvents;

    public SensorState(HardwareSensorInfo s) {
        this.hardwareSensorInfo = s;
    }

    private SensorState(Parcel in) {
        this.name = in.readString();

        // порядок полей соответствует порядку бит от старшего к младшим
        int value = in.readInt();
        byte sensorNumber = (byte) (value & 0xFF);
        value >>= 8;
        int button = value & 0b11;
        value >>= 2;
        boolean batteryHigh = (value & 1) != 0;
        value >>= 1;
        this.hardwareSensorInfo = new HardwareSensorInfo(sensorNumber, button, batteryHigh);
        //
        boolean isAlert = (value & 1) != 0;
        value >>= 1;
        this.isAlert = isAlert;
        //
        int stateNumber = value & 0b111;
        value >>= 3;
        this.state = stateNumber == 0 ? null : State.values()[stateNumber - 1];
        //
        int sensorTypeNumber = value & 0b11;
        value >>= 2;
        this.sensorTypeNumber = sensorTypeNumber;
        //
        for (int i = 0; i < 4; i++) {
            int action = value & 0b111;
            value >>= 3;
            this.setAction(i, action == 0 ? null : ActionType.values()[action - 1]);
        }

//        this.hardwareSensorInfo = in.readParcelable(HardwareSensorInfo.class.getClassLoader());
//        this.isAlert = in.readByte() != 0;
//        int tmpState = in.readInt();
//        this.state = tmpState == -1 ? null : State.values()[tmpState];

        this.guid = in.readString();
        this.recentEvents = new ArrayList<>();
        in.readList(this.recentEvents, getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);

        int value = 0;
        //
        if (BuildConfig.DEBUG && !(ActionType.values().length <= 7))
            throw new AssertionError();
        for (int i = 3; i >= 0; i--) {
            value <<= 3;
            ActionType actionType = getAction(i);
            value |= actionType == null ? 0 : actionType.ordinal() + 1;
        }
        //
        if (BuildConfig.DEBUG && !(sensorTypeNumber <= 3))
            throw new AssertionError();
        value <<= 2;
        value |= sensorTypeNumber;
        //
        if (BuildConfig.DEBUG && !(State.values().length <= 7))
            throw new AssertionError();
        value <<= 3;
        value |= state == null ? 0 : state.ordinal() + 1;
        //
        value <<= 1;
        value |= isAlert ? 1 : 0;
        //
        value <<= 1;
        value |= hardwareSensorInfo.isBatteryHigh() ? 1 : 0;
        if (BuildConfig.DEBUG && !(hardwareSensorInfo.getButton() <= 3))
            throw new AssertionError();
        value <<= 2;
        value |= hardwareSensorInfo.getButton();
        value <<= 8;
        if (BuildConfig.DEBUG && !(hardwareSensorInfo.getSensorNumber() <= 255))
            throw new AssertionError();
        value |= hardwareSensorInfo.getSensorNumber();

        dest.writeInt(value);

//        dest.writeParcelable(this.hardwareSensorInfo, 0);
//        dest.writeByte(isAlert ? (byte) 1 : (byte) 0);
//        dest.writeInt(this.state == null ? -1 : this.state.ordinal());

        dest.writeString(this.guid);
        dest.writeList(this.recentEvents);
    }

    public int getSensorTypeNumber() {
        return sensorTypeNumber;
    }

    public void setSensorTypeNumber(int sensorTypeNumber) {
        this.sensorTypeNumber = sensorTypeNumber;
    }

    public ActionType getAction(int buttonIndex /* нумерация от 0 */) {
        switch (buttonIndex) {
            case 1:
                return b2Action;
            case 2:
                return b3Action;
            case 3:
                return b4Action;
            default:
                return b1Action;
        }
    }

    public void setAction(int buttonIndex /* нумерация от 0 */, ActionType action) {
        switch (buttonIndex) {
            case 1:
                b2Action = action;
                break;
            case 2:
                b3Action = action;
                break;
            case 3:
                b4Action = action;
                break;
            default:
                b1Action = action;
        }
    }

    public String getGuid() {
        if (guid == null)
            guid = Utils.genGuid();
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public State getState() {
        if (state == null)
            return State.NOT_INIT;
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getValidName(Context c) {
        return name != null && name.trim().length() > 0 ? name : c.getString(R.string.sensor_name_is_empty);
    }

    public HardwareSensorInfo getHardwareSensorInfo() {
        return hardwareSensorInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAlert() {
        return isAlert;
    }

    public void setAlert(boolean isAlert) {
        this.isAlert = isAlert;
    }

    public boolean isActive() {
        return State.INIT_ACTIVE.equals(state);
    }

    public void setActive(boolean isActive) {
        state = isActive ? State.INIT_ACTIVE : State.INIT_NOT_ACTIVE;
    }

    private List<Date> getLeadEvents() {
        if (recentEvents.size() > MAX_RECENT_EVENTS_COUNT)
            return recentEvents.subList(0, MAX_RECENT_EVENTS_COUNT);
        else
            return recentEvents;
    }

    public List<Date> getRecentEvents() {
        if (recentEvents == null) {
            recentEvents = new ArrayList<>();
            SingleSensorEventsReader reader = new SingleSensorEventsReader(this);
            boolean alert = false;
            try {
                while (!reader.eof()) {
                    EventStorage.Event event = reader.next();
                    if (event == null) {
                        Utils.log("Получен пустой event!!!");
                        continue;
                    }
                    if (EventType.READ.equals(event.getEventType())) {
                        break;
                    }
                    if (recentEvents.size() == 0 && EventType.ALERT.equals(event.getEventType())) {
                        alert = true;
                    }
                    recentEvents.add(event.getEventTime());
                    if (recentEvents.size() > MAX_RECENT_EVENTS_COUNT)
                        break;
                }
            } finally {
                reader.close();
            }
            setAlert(alert);
        }
        return getLeadEvents();
    }

    public boolean hasMoreEvents() {
        return recentEvents != null && recentEvents.size() > MAX_RECENT_EVENTS_COUNT;
    }

    public void saveEvent(Date d, EventType eventType) {
        EventStorage.saveEvent(getGuid(), d, eventType);
        reloadEvents();
    }

    void reloadEvents() {
        recentEvents = null;
        getRecentEvents();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "SensorState{" +
                "name='" + name + '\'' +
                ", hardwareSensorInfo=" + hardwareSensorInfo +
                ", isAlert=" + isAlert +
                ", state=" + state +
                ", guid='" + guid + '\'' +
                ", recentEvents=" + recentEvents +
                '}';
    }

    public void setSensorType(int sensorTypeNumber, int action1, int action2, int action3, int action4) {
        this.sensorTypeNumber = sensorTypeNumber;
        this.b1Action = ActionType.values()[action1];
        this.b2Action = ActionType.values()[action2];
        this.b3Action = ActionType.values()[action3];
        this.b4Action = ActionType.values()[action4];
    }

    public static enum State implements Serializable {
        NOT_INIT, IN_INIT_PROCESS, STEP1, STEP2, INIT_NOT_ACTIVE, INIT_ACTIVE
    }
}
