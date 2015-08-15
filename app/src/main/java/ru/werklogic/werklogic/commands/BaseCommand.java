package ru.werklogic.werklogic.commands;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import ru.werklogic.werklogic.dm.DataModel;

/**
 * Created by bmw on 19.07.2015.
 */
public class BaseCommand implements Parcelable {
    protected DataModel dataModel;
    protected Context context;

    public void setEnvironment(DataModel dataModel, Context context) {
        this.dataModel = dataModel;
        this.context = context;
    }

    /**
     * Выполнение команды локально перед отсылкой в облако на планшете с "хвостом".
     * @return команда, которую необходимо послать по сети в облако или null, если в посылке команды в облако нет необходимости
     */
    public BaseCommand executeLocalOnInternalConfig() {
        executeOnInternalConfig();
        return null;
    }

    /**
     * Выполнение команды локально перед отсылкой в облако на планшете без "хвоста".
     * @return команда, которую необходимо послать по сети в облако или null, если в посылке команды в облако нет необходимости
     */
    public BaseCommand executeLocalOnExternalConfig() {
        return this;
    }

    /**
     * Исполнение команды при приходе из облака на планшете с "хвостом".
     */
    public void executeOnInternalConfig() {

    }

    /**
     * Исполнение команды при приходе из облака на планшете без "хвоста".
     */
    public void executeOnExternalConfig() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public BaseCommand() {
    }

    private BaseCommand(Parcel in) {
    }

    @Override
    public String toString() {
        return BaseCommand.class.getSimpleName()+"{}";
    }
}
