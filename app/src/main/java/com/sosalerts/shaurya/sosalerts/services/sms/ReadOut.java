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




public class ReadOut extends IntentService /*implements TextToSpeech.OnInitListener*/{


    boolean paused = false;
    String leftToRead = null;
    String res = null;
    private final String fileName = this.getClass().getSimpleName();
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

    private TextToSpeech myTTS;
    private String whatToSay;

    @Override
    protected void onHandleIntent(Intent intent) {


        String textToSpeak = intent.getStringExtra(MainActivity.textToSpeak);
        String orignator = intent.getStringExtra(MainActivity.orignationActivityName);
        if(null == textToSpeak){
            textToSpeak = "Hello";
        }


        sayString(textToSpeak);
        if(IncomingSms.findMyPhone.equals(orignator)){
            MainActivity.phoneFound = false;
            Intent dialogIntent = new Intent(this, MyDialog.class);
            dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);
            while(!MainActivity.phoneFound){

                Log.e(fileName, " MainActivity.phoneFound  "+MainActivity.phoneFound );
                sayString(textToSpeak);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


    }
    public void sayString(String speak) {
        whatToSay = speak;
        myTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.e(fileName, "Text to speach  Status Read Out ##### $$$$$$$$$$$$$$$$$ "+status);
                String utteranceId=this.hashCode() + "";
                myTTS.speak(whatToSay, 1, null, utteranceId);
            }
        });
        if (MainActivity.myTTS != null) {


        }


    }

    @Override
    public void onDestroy() {

    super.onDestroy();

}



}
