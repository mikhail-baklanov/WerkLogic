package ru.werklogic.werklogic.protocol.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HardwareSensorInfo implements IResponseData, Parcelable, Serializable {

    public static final Parcelable.Creator<HardwareSensorInfo> CREATOR = new Parcelable.Creator<HardwareSensorInfo>() {

        @Override
        public HardwareSensorInfo createFromParcel(Parcel source) {
            byte sensorNumber = source.readByte();
            int button = source.readInt();
            boolean batteryHigh = source.readInt() == 0 ? false : true;
            HardwareSensorInfo sensor = new HardwareSensorInfo(sensorNumber, button, batteryHigh);
            return sensor;
        }

        @Override
        public HardwareSensorInfo[] newArray(int size) {
            return new HardwareSensorInfo[size];
        }
    };
    private byte sensorNumber;
    private int button;
    private boolean batteryHigh;

    public HardwareSensorInfo(byte sensorNumber, int button, boolean batteryHigh) {
        this.sensorNumber = sensorNumber;
        this.button = button;
        this.batteryHigh = batteryHigh;
    }

    public byte getSensorNumber() {
        return sensorNumber;
    }

    public int getButton() {
        return button;
    }

    public void setButton(int button) {
        this.button = button;
    }

    public boolean isBatteryHigh() {
        return batteryHigh;
    }

    public void setBatteryHigh(boolean batteryHigh) {
        this.batteryHigh = batteryHigh;
    }

    @Override
    public String toString() {
        return "HardwareSensorInfo {sensorNumber=" + sensorNumber + ", button=" + button + ", batteryHigh="
                + batteryHigh + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(sensorNumber);
        dest.writeInt(button);
        dest.writeInt(batteryHigh ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof HardwareSensorInfo) {
            HardwareSensorInfo v = (HardwareSensorInfo) o;
            return sensorNumber == v.sensorNumber && button == v.button && batteryHigh == v.batteryHigh;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ("" + sensorNumber + ";" + button + ";" + batteryHigh).hashCode();
    }
}
