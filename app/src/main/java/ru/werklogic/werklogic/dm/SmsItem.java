package ru.werklogic.werklogic.dm;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by bmw on 13.05.2015.
 */
public class SmsItem implements Serializable, Parcelable {
    private String number;
    private boolean active;

    public SmsItem(String number, boolean active) {
        this.number = number;
        this.active = active;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.number);
        dest.writeByte(active ? (byte) 1 : (byte) 0);
    }

    private SmsItem(Parcel in) {
        this.number = in.readString();
        this.active = in.readByte() != 0;
    }

    public static final Parcelable.Creator<SmsItem> CREATOR = new Parcelable.Creator<SmsItem>() {
        public SmsItem createFromParcel(Parcel source) {
            return new SmsItem(source);
        }

        public SmsItem[] newArray(int size) {
            return new SmsItem[size];
        }
    };

    @Override
    public String toString() {
        return "SmsItem{" +
                "number='" + number + '\'' +
                ", active=" + active +
                '}';
    }
}
