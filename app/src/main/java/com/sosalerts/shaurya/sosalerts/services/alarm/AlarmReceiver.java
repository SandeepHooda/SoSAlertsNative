package com.sosalerts.shaurya.sosalerts.services.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.util.GetLocationCordinatesService;

/**
 * Created by shaurya on 1/26/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private final String fileName = this.getClass().getSimpleName();
    public static final String originator = "AlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {


        if(Boolean.parseBoolean(Storage.getFromDB(Storage.settingsLocationAutoUpdates,context))){
            Intent locationCordinatesIntent = new Intent(context, GetLocationCordinatesService.class);
            context.startService(locationCordinatesIntent);
        }else {
            Log.e(fileName, "Alarm service to find location is disabled");
        }


    }
}