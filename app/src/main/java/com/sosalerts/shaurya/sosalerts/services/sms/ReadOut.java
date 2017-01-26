package com.sosalerts.shaurya.sosalerts.services.sms;

/**
 * Created by shaurya on 1/26/2017.
 */

import android.app.AlertDialog;
import android.app.IntentService;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.content.DialogInterface.OnClickListener;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.util.MyDialog;

import java.util.Locale;


public class ReadOut extends IntentService /*implements TextToSpeech.OnInitListener*/{


    boolean paused = false;
    String leftToRead = null;
    String res = null;
    public ReadOut() {
        super("SpeechService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




    @Override
    protected void onHandleIntent(Intent intent) {

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        MainActivity.phoneFound = false;
        Intent dialogIntent = new Intent(this, MyDialog.class);
        dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);// to get user cordinates
        int i=0;
        while(!MainActivity.phoneFound){
            i++;
            Log.e("LOB", " MainActivity.phoneFound  "+MainActivity.phoneFound );
            sayString("I am here");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    public void sayString(String string) {
        if (MainActivity.myTTS != null) {
            String utteranceId=this.hashCode() + "";
            MainActivity.myTTS.speak(string, 1, null, utteranceId);

        }


    }

    @Override
    public void onDestroy() {

    super.onDestroy();

}



}
