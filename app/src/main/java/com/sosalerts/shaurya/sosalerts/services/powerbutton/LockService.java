package com.sosalerts.shaurya.sosalerts.services.powerbutton;

/**
 * Created by shaurya on 1/21/2017.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.sosalerts.shaurya.sosalerts.services.alarm.AlarmReceiver;

/*
This service receive intent of scren on/off/unlock
 */
public class LockService extends Service {
    private static boolean lockSericeStatted = false;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private final String fileName = this.getClass().getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!lockSericeStatted){
            Log.e(fileName, " Power listener started With Alarm" );
            final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            final BroadcastReceiver screenLockReceiver = new ScreenReceiver();
            registerReceiver(screenLockReceiver, filter);
            lockSericeStatted = true;

            // Hopefully your alarm will have a lower frequency than this!
            alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
            Intent intentForAlrrm = new Intent(this, AlarmReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(this, 0, intentForAlrrm, 0);
            alarmMgr.cancel(alarmIntent);
            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + (10 * 1000),
                    (10 * 1000), alarmIntent);

        }
        return super.onStartCommand(intent, flags, startId);
    }
    public class LocalBinder extends Binder {
        LockService getService() {
            return LockService.this;
        }
    }
}