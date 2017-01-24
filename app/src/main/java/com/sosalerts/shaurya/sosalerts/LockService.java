package com.sosalerts.shaurya.sosalerts;

/**
 * Created by shaurya on 1/21/2017.
 */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/*
This service receive intent of scren on/off/unlock
 */
public class LockService extends Service {
    private static boolean lockSericeStatted = false;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!lockSericeStatted){
            Log.e("LOB", " Power listener started " );
            final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            final BroadcastReceiver screenLockReceiver = new ScreenReceiver();
            registerReceiver(screenLockReceiver, filter);
            lockSericeStatted = true;

        }
        return super.onStartCommand(intent, flags, startId);
    }
    public class LocalBinder extends Binder {
        LockService getService() {
            return LockService.this;
        }
    }
}