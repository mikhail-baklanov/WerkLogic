package ru.werklogic.werklogic.marshalling;

import android.os.Parcelable;

import ru.werklogic.werklogic.commands.AddSmsCommand;
import ru.werklogic.werklogic.commands.BaseCommand;
import ru.werklogic.werklogic.commands.ConnectNewClientCommand;
import ru.werklogic.werklogic.commands.DeleteSmsCommand;
import ru.werklogic.werklogic.commands.SpyModeSwitchCommand;
import ru.werklogic.werklogic.commands.UpdateConfigCommand;
import ru.werklogic.werklogic.commands.UpdateSensorActivityCommand;
import ru.werklogic.werklogic.commands.UpdateSensorCommand;
import ru.werklogic.werklogic.commands.UpdateSmsCommand;

/**
 * Created by bmw on 20.07.2015.
 */
public class Packer {

    public static byte[] marshall(Parcelable command) {
        Envelope envelope = new Envelope(command.getClass().getSimpleName(), ParcelableUtils.marshall(command));
        byte[] result = ParcelableUtils.marshall(envelope);
        return result;
    }

    public static BaseCommand unmarshall(byte[] bytes) {
        Envelope envelope = ParcelableUtils.unmarshall(bytes, Envelope.CREATOR);
        bytes = envelope.getBytes();
        String className = envelope.getClazz();
        if (AddSmsCommand.class.getSimpleName().equals(className)) {
            return ParcelableUtils.unmarshall(bytes, AddSmsCommand.CREATOR);
        }
        if (ConnectNewClientCommand.class.getSimpleName().equals(className)) {
            return ParcelableUtils.unmarshall(bytes, ConnectNewClientCommand.CREATOR);
        }
        if (DeleteSmsCommand.class.getSimpleName().equals(className)) {
            return ParcelableUtils.unmarshall(bytes, DeleteSmsCommand.CREATOR);
        }
        if (SpyModeSwitchCommand.class.getSimpleName().equals(className)) {
            return ParcelableUtils.unmarshall(bytes, SpyModeSwitchCommand.CREATOR);
        }
        if (UpdateConfigCommand.class.getSimpleName().equals(className)) {
            return ParcelableUtils.unmarshall(bytes, UpdateConfigCommand.CREATOR);
        }
        if (UpdateSensorActivityCommand.class.getSimpleName().equals(className)) {
            return ParcelableUtils.unmarshall(bytes, UpdateSensorActivityCommand.CREATOR);
        }
        if (UpdateSensorCommand.class.getSimpleName().equals(className)) {
            return ParcelableUtils.unmarshall(bytes, UpdateSensorCommand.CREATOR);
        }
        if (UpdateSmsCommand.class.getSimpleName().equals(className)) {
            return ParcelableUtils.unmarshall(bytes, UpdateSmsCommand.CREATOR);
        }
        // ...
        return null;
    }
}
