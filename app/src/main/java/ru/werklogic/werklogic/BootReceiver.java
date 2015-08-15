package ru.werklogic.werklogic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.werklogic.werklogic.utils.Utils;

public class BootReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Utils.log(BootReceiver.class.getSimpleName() + ": получен intent=" + intent);

        Utils.startService(context);
    }
}
