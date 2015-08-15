package ru.werklogic.werklogic.marshalling;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by bmw on 20.07.2015.
 */
class Envelope implements Parcelable {
    private String clazz;
    private byte[] baseCommandBytes;
    public Envelope(String clazz, byte[] baseCommand) {
        this.clazz = clazz;
        this.baseCommandBytes = baseCommand;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.clazz);
        dest.writeByteArray(this.baseCommandBytes);
    }

    private Envelope(Parcel in) {
        this.clazz = in.readString();
        this.baseCommandBytes = in.createByteArray();
    }

    public static final Parcelable.Creator<Envelope> CREATOR = new Parcelable.Creator<Envelope>() {
        public Envelope createFromParcel(Parcel source) {
            return new Envelope(source);
        }

        public Envelope[] newArray(int size) {
            return new Envelope[size];
        }
    };

    public String getClazz() {
        return clazz;
    }

    public byte[] getBytes() {
        return baseCommandBytes;
    }

}

