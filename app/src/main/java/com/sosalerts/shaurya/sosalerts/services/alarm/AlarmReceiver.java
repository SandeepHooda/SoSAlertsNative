package com.sosalerts.shaurya.sosalerts.services.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.services.util.GetLocationCordinatesService;

/**
 * Created by shaurya on 1/26/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private final String fileName = this.getClass().getSimpleName();
    public static final String originator = "AlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(fileName, "Alarm service With address ");


        Intent locationCordinatesIntent = new Intent(context, GetLocationCordinatesService.class);
        locationCordinatesIntent.putExtra(MainActivity.orignationActivityName, originator+(Math.random()));
        //locationCordinatesIntent.putExtra(GetLocationCordinatesService.ADDRESS_RESULT_RECEIVER, new LocationTrackerIntentService());
        context.startService(locationCordinatesIntent);

    }
}