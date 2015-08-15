package ru.werklogic.werklogic.commands;

import android.os.Parcel;

import ru.werklogic.werklogic.dm.SmsItem;

/**
 * Created by bmw on 14.08.2015.
 */
public class AddSmsCommand extends BaseCommand implements android.os.Parcelable {
    private SmsItem smsItem;

    public AddSmsCommand(SmsItem smsItem) {
        this.smsItem = smsItem;
    }

    @Override
    public void executeOnInternalConfig() {
        dataModel.addSms(smsItem);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.smsItem, 0);
    }

    private AddSmsCommand(Parcel in) {
        this.smsItem = in.readParcelable(SmsItem.class.getClassLoader());
    }

    public static final Creator<AddSmsCommand> CREATOR = new Creator<AddSmsCommand>() {
        public AddSmsCommand createFromParcel(Parcel source) {
            return new AddSmsCommand(source);
        }

        public AddSmsCommand[] newArray(int size) {
            return new AddSmsCommand[size];
        }
    };

    @Override
    public String toString() {
        return "AddSmsCommand{" +
                "smsItem=" + smsItem +
                '}';
    }
}
