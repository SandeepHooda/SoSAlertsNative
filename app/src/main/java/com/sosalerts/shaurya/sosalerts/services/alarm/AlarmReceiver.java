package com.sosalerts.shaurya.sosalerts.services.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.services.sms.ReadOut;
import com.sosalerts.shaurya.sosalerts.services.util.GetLocationCordinatesActivity;

/**
 * Created by shaurya on 1/26/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private final String fileName = this.getClass().getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(fileName, "Alarm service With address ");

        /*Intent speakIntent = new Intent(context, ReadOut.class);
        speakIntent.putExtra(MainActivity.textToSpeak,"Fire");
        context.startService(speakIntent);*/
        Intent locationCordinatesIntent = new Intent(context, GetLocationCordinatesActivity.class);
        context.startService(locationCordinatesIntent);

    }
}