package com.sosalerts.shaurya.sosalerts.services.sms;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.db.Storage;

import java.util.Locale;

/**
 * Created by shaurya on 1/26/2017.
 */

public class IncomingSms extends BroadcastReceiver {
    public static final String findMyPhone = "findMyPhone";
    public static final String whereAreYou = "WhereAreYou";
    public static final String phoneNo = "";
    // Get the object of SmsManager
    final SmsManager sms = SmsManager.getDefault();
    private final String fileName = this.getClass().getSimpleName();
    private boolean phoneFound = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i], bundle.getString("format"));
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();

                    Log.i("SmsReceiver", "senderNum: "+ senderNum + "; message: " + message);

                    if (null != message){
                        message = message.toLowerCase().trim();
                        message.replaceAll("\\?","");
                    }
                    if("fmp".equals(message) || "find my phone".equals(message)){
                        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
                        Intent mainActivityIntent = new Intent(context, MainActivity.class);
                        mainActivityIntent.putExtra(MainActivity.orignationActivityName, findMyPhone);
                        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(mainActivityIntent);// to get user cordinates
                    }
                    if("wru".equals(message) || "where are you".equals(message)){
                        Intent mainActivityIntent = new Intent(context, MainActivity.class);
                        mainActivityIntent.putExtra(MainActivity.orignationActivityName, whereAreYou);
                        mainActivityIntent.putExtra(phoneNo, senderNum);
                        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(mainActivityIntent);// to get user cordinates

                    }
                    int duration = Toast.LENGTH_LONG;
                    Toast.makeText(context, "senderNum: "+ senderNum + ", message: " + message, duration).show();




                    // iAmHere( context);

                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(fileName, "Exception smsReceiver ::: " +e);

        }
    }



}
