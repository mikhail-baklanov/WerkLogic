package ru.werklogic.werklogic.commands;

import android.os.Parcel;

import ru.werklogic.werklogic.dm.SmsItem;

/**
 * Created by bmw on 14.08.2015.
 */
public class UpdateSmsCommand extends BaseCommand implements android.os.Parcelable {
    private SmsItem item;
    private String value;

    public UpdateSmsCommand(SmsItem item, String value) {
        this.item = item;
        this.value = value;
    }

    @Override
    public void executeOnInternalConfig() {
        dataModel.setSmsNumber(item, value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.item, 0);
        dest.writeString(this.value);
    }

    private UpdateSmsCommand(Parcel in) {
        this.item = in.readParcelable(SmsItem.class.getClassLoader());
        this.value = in.readString();
    }

    public static final Creator<UpdateSmsCommand> CREATOR = new Creator<UpdateSmsCommand>() {
        public UpdateSmsCommand createFromParcel(Parcel source) {
            return new UpdateSmsCommand(source);
        }

        public UpdateSmsCommand[] newArray(int size) {
            return new UpdateSmsCommand[size];
        }
    };

    @Override
    public String toString() {
        return "UpdateSmsCommand{" +
                "item=" + item +
                ", value='" + value + '\'' +
                '}';
    }
}
