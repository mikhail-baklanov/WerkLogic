package ru.werklogic.werklogic.commands;

import android.os.Parcel;

import ru.werklogic.werklogic.dm.SmsItem;

/**
 * Created by bmw on 14.08.2015.
 */
public class DeleteSmsCommand extends BaseCommand implements android.os.Parcelable {
    private SmsItem item;
    public DeleteSmsCommand(SmsItem item) {
        this.item = item;
    }

    @Override
    public void executeOnInternalConfig() {
        dataModel.deleteSms(item);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.item, 0);
    }

    private DeleteSmsCommand(Parcel in) {
        this.item = in.readParcelable(SmsItem.class.getClassLoader());
    }

    public static final Creator<DeleteSmsCommand> CREATOR = new Creator<DeleteSmsCommand>() {
        public DeleteSmsCommand createFromParcel(Parcel source) {
            return new DeleteSmsCommand(source);
        }

        public DeleteSmsCommand[] newArray(int size) {
            return new DeleteSmsCommand[size];
        }
    };

    @Override
    public String toString() {
        return "DeleteSmsCommand{" +
                "item=" + item +
                '}';
    }
}
