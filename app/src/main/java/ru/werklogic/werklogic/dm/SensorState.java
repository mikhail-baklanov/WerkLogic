package ru.werklogic.werklogic.dm;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private static final int MAX_RECENT_EVENTS_COUNT = 4;
    private String name;
    private HardwareSensorInfo hardwareSensorInfo;
    private boolean isAlert;
    private State state;

    public void setGuid(String guid) {
        this.guid = guid;
    }

    private String guid;
    private List<Date> recentEvents;

    public SensorState(HardwareSensorInfo s) {
        this.hardwareSensorInfo = s;
    }


    public String getGuid() {
        if (guid == null)
            guid = Utils.genGuid();
        return guid;
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

    public static enum State implements Serializable {
        NOT_INIT, IN_INIT_PROCESS, STEP1, STEP2, INIT_NOT_ACTIVE, INIT_ACTIVE
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeParcelable(this.hardwareSensorInfo, 0);
        dest.writeByte(isAlert ? (byte) 1 : (byte) 0);
        dest.writeInt(this.state == null ? -1 : this.state.ordinal());
        dest.writeString(this.guid);
        dest.writeList(this.recentEvents);
    }

    private SensorState(Parcel in) {
        this.name = in.readString();
        this.hardwareSensorInfo = in.readParcelable(HardwareSensorInfo.class.getClassLoader());
        this.isAlert = in.readByte() != 0;
        int tmpState = in.readInt();
        this.state = tmpState == -1 ? null : State.values()[tmpState];
        this.guid = in.readString();
        this.recentEvents = new ArrayList<>();
        in.readList(this.recentEvents, getClass().getClassLoader());
    }

    public static final Parcelable.Creator<SensorState> CREATOR = new Parcelable.Creator<SensorState>() {
        public SensorState createFromParcel(Parcel source) {
            return new SensorState(source);
        }

        public SensorState[] newArray(int size) {
            return new SensorState[size];
        }
    };

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
}
