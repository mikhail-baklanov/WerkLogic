package ru.werklogic.werklogic.commands;

import android.os.Parcel;

/**
 * Created by bmw on 14.08.2015.
 */
public class UpdateSpyModeCommand extends BaseCommand implements android.os.Parcelable {
    private boolean spyMode;

    public UpdateSpyModeCommand(boolean spyMode) {
        this.spyMode = spyMode;
    }

    @Override
    public BaseCommand executeLocalOnInternalConfig() {
        if (dataModel.isSpyMode() && !spyMode)
            dataModel.setSpyMode(spyMode);
        else
        if (!dataModel.isSpyMode() && spyMode)
            dataModel.scheduleSpyMode();
        return null;
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

    private UpdateSpyModeCommand(Parcel in) {
        this.spyMode = in.readByte() != 0;
    }

    public static final Creator<UpdateSpyModeCommand> CREATOR = new Creator<UpdateSpyModeCommand>() {
        public UpdateSpyModeCommand createFromParcel(Parcel source) {
            return new UpdateSpyModeCommand(source);
        }

        public UpdateSpyModeCommand[] newArray(int size) {
            return new UpdateSpyModeCommand[size];
        }
    };

    @Override
    public String toString() {
        return "SpyModeSwitchCommand{" +
                "spyMode=" + spyMode +
                '}';
    }
}
