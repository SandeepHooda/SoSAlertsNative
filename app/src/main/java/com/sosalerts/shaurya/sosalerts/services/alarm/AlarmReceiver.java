package com.sosalerts.shaurya.sosalerts.services.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.util.GetLocationCordinatesService;
import com.sosalerts.shaurya.sosalerts.services.util.ReadOut;

/**
 * Created by shaurya on 1/26/2017.
 */

public class AlarmReceiver extends WakefulBroadcastReceiver {
    private final String fileName = this.getClass().getSimpleName();
    public static final String originator = "AlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {


        if(Storage.isLocationTrackerOn(context)){
            if (Boolean.parseBoolean(Storage.getFromDB(Storage.speakLocation,context ))) {
                Intent speakIntent = new Intent(context, ReadOut.class);
                speakIntent.putExtra(ReadOut.textToSpeak,"Getting location");//+" accuracy "+mLastLocation.getAccuracy()
                speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
                context.startService(speakIntent);
            }
            Intent locationCordinatesIntent = new Intent(context, GetLocationCordinatesService.class);
            startWakefulService(context,locationCordinatesIntent);
        }else {
            Log.e(fileName, "Alarm service to find location is disabled");
        }


    }
}