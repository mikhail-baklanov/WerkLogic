package ru.werklogic.werklogic.commands;

import android.os.Parcel;

/**
 * Created by bmw on 14.08.2015.
 */
public class SpyModeSwitchCommand extends BaseCommand implements android.os.Parcelable {
    private boolean spyMode;

    public SpyModeSwitchCommand(boolean spyMode) {
        this.spyMode = spyMode;
    }

    @Override
    public void executeOnInternalConfig() {
        dataModel.setSpyMode(spyMode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(spyMode ? (byte) 1 : (byte) 0);
    }

    private SpyModeSwitchCommand(Parcel in) {
        this.spyMode = in.readByte() != 0;
    }

    public static final Creator<SpyModeSwitchCommand> CREATOR = new Creator<SpyModeSwitchCommand>() {
        public SpyModeSwitchCommand createFromParcel(Parcel source) {
            return new SpyModeSwitchCommand(source);
        }

        public SpyModeSwitchCommand[] newArray(int size) {
            return new SpyModeSwitchCommand[size];
        }
    };

    @Override
    public String toString() {
        return "SpyModeSwitchCommand{" +
                "spyMode=" + spyMode +
                '}';
    }
}
