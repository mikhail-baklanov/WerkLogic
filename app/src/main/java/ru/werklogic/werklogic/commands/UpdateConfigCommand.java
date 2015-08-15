package ru.werklogic.werklogic.commands;

import android.os.Parcel;

import ru.werklogic.werklogic.dm.Config;

/**
 * Created by bmw on 25.07.2015.
 */
public class UpdateConfigCommand extends BaseCommand implements android.os.Parcelable {

    private Config config;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.dataModel.getConfig(), flags);
    }

    public UpdateConfigCommand() {
    }

    private UpdateConfigCommand(Parcel in) {
        config = in.readParcelable(Config.class.getClassLoader());
    }

    public BaseCommand executeLocalOnInternalConfig() {
        return this;
    }

    public BaseCommand executeLocalOnExternalConfig() {
        return null;
    }

    @Override
    public void executeOnExternalConfig() {
        this.dataModel.setExternalConfig(config);
    }

    public static final Creator<UpdateConfigCommand> CREATOR = new Creator<UpdateConfigCommand>() {
        public UpdateConfigCommand createFromParcel(Parcel source) {
            return new UpdateConfigCommand(source);
        }

        public UpdateConfigCommand[] newArray(int size) {
            return new UpdateConfigCommand[size];
        }
    };

    @Override
    public String toString() {
        return "UpdateConfigCommand{" +
                "config=" + config +
                '}';
    }
}
