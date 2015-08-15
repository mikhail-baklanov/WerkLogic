package ru.werklogic.werklogic.commands;

import android.os.Parcel;

import ru.werklogic.werklogic.protocol.data.HardwareSensorInfo;

/**
 * Created by bmw on 21.07.2015.
 */
public class UpdateSensorActivityCommand extends BaseCommand implements android.os.Parcelable {
    private HardwareSensorInfo hardwareSensorInfo;
    private boolean isActive;

    public UpdateSensorActivityCommand(HardwareSensorInfo hardwareSensorInfo, boolean isActive) {

        this.hardwareSensorInfo = hardwareSensorInfo;
        this.isActive = isActive;
    }

    @Override
    public void executeOnInternalConfig() {
        dataModel.setActive(hardwareSensorInfo, isActive);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.hardwareSensorInfo, 0);
        dest.writeByte(isActive ? (byte) 1 : (byte) 0);
    }

    private UpdateSensorActivityCommand(Parcel in) {
        this.hardwareSensorInfo = in.readParcelable(HardwareSensorInfo.class.getClassLoader());
        this.isActive = in.readByte() != 0;
    }

    public static final Creator<UpdateSensorActivityCommand> CREATOR = new Creator<UpdateSensorActivityCommand>() {
        public UpdateSensorActivityCommand createFromParcel(Parcel source) {
            return new UpdateSensorActivityCommand(source);
        }

        public UpdateSensorActivityCommand[] newArray(int size) {
            return new UpdateSensorActivityCommand[size];
        }
    };

    @Override
    public String toString() {
        return "UpdateSensorActivityCommand{" +
                "hardwareSensorInfo=" + hardwareSensorInfo +
                ", isActive=" + isActive +
                '}';
    }
}
