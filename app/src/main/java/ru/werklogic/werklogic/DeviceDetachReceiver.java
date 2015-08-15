package ru.werklogic.werklogic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.werklogic.werklogic.dm.DataModel;
import ru.werklogic.werklogic.utils.Utils;

/**
 * Created by bmw on 16.07.2015.
 */
public class DeviceDetachReceiver extends BroadcastReceiver {
    public void onReceive(final Context context, Intent intent) {
        Utils.log(DeviceDetachReceiver.class.getSimpleName() + ": получен intent=" + intent);
        sendDetachEvent(context);

    }
    private void sendDetachEvent(Context c) {
        Intent intent = new Intent(DataModel.DETACH_ACTION);
        c.sendBroadcast(intent);
    }

}