package ru.werklogic.werklogic.commands;

import android.content.Intent;
import android.os.Parcel;

import ru.werklogic.werklogic.service.AutoUnbindServiceConnection;
import ru.werklogic.werklogic.service.IWerkLogicService;
import ru.werklogic.werklogic.service.RunnableWithParameter;
import ru.werklogic.werklogic.service.WerkLogicService;

/**
 * Created by bmw on 25.07.2015.
 */
public class ConnectNewClientCommand extends BaseCommand implements android.os.Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public ConnectNewClientCommand() {
    }

    private ConnectNewClientCommand(Parcel in) {
    }

    @Override
    public void executeOnInternalConfig() {
        AutoUnbindServiceConnection connection = new AutoUnbindServiceConnection(context, new RunnableWithParameter<IWerkLogicService>() {
            @Override
            public void run(IWerkLogicService service) {
                service.processCommand(new UpdateConfigCommand());
            }
        });
        context.bindService(new Intent(context, WerkLogicService.class), connection, 0);
    }

    public static final Creator<ConnectNewClientCommand> CREATOR = new Creator<ConnectNewClientCommand>() {
        public ConnectNewClientCommand createFromParcel(Parcel source) {
            return new ConnectNewClientCommand(source);
        }

        public ConnectNewClientCommand[] newArray(int size) {
            return new ConnectNewClientCommand[size];
        }
    };

    @Override
    public String toString() {
        return ConnectNewClientCommand.class.getSimpleName() + "{}";
    }
}
