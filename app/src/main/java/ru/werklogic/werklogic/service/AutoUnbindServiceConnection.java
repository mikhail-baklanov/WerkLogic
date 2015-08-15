package ru.werklogic.werklogic.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import ru.werklogic.werklogic.utils.Utils;

/**
 * Created by bmw on 14.07.2015.
 */
public class AutoUnbindServiceConnection implements ServiceConnection {
    private Context c;
    private RunnableWithParameter<IWerkLogicService> runnable;

    public AutoUnbindServiceConnection(Context c, RunnableWithParameter<IWerkLogicService> runnable) {
        this.c = c;
        this.runnable = runnable;
    }

    public void onServiceConnected(ComponentName name, IBinder binder) {
        Utils.log("onServiceConnected");
        IWerkLogicService service = ((WerkLogicService.WerkLogicServiceBinder) binder).getService();
        runnable.run(service);
        c.unbindService(this);
    }

    public void onServiceDisconnected(ComponentName name) {
        Utils.log("onServiceDisconnected");
    }
}

