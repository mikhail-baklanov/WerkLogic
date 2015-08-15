package ru.werklogic.werklogic.commands;

import android.os.Parcel;

import ru.werklogic.werklogic.protocol.data.HardwareSensorInfo;

/**
 * Created by bmw on 21.07.2015.
 */
public class UpdateSensorCommand extends BaseCommand implements android.os.Parcelable {
    private HardwareSensorInfo hardwareSensorInfo;
    private String sensorName;

    public UpdateSensorCommand(HardwareSensorInfo hardwareSensorInfo, String sensorName) {
        this.hardwareSensorInfo = hardwareSensorInfo;
        this.sensorName = sensorName;
    }

    @Override
    public void executeOnInternalConfig() {
        dataModel.setSensorName(hardwareSensorInfo, sensorName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.hardwareSensorInfo, 0);
        dest.writeString(this.sensorName);
    }

    private UpdateSensorCommand(Parcel in) {
        this.hardwareSensorInfo = in.readParcelable(HardwareSensorInfo.class.getClassLoader());
        this.sensorName = in.readString();
    }

    public static final Creator<UpdateSensorCommand> CREATOR = new Creator<UpdateSensorCommand>() {
        public UpdateSensorCommand createFromParcel(Parcel source) {
            return new UpdateSensorCommand(source);
        }

        public UpdateSensorCommand[] newArray(int size) {
            return new UpdateSensorCommand[size];
        }
    };

    @Override
    public String toString() {
        return "UpdateSensorCommand {" +
                "hardwareSensorInfo=" + hardwareSensorInfo +
                ", sensorName='" + sensorName + '\'' +
                '}';
    }
}
