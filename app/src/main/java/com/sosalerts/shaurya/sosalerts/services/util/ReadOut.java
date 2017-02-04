package com.sosalerts.shaurya.sosalerts.services.util;

/**
 * Created by shaurya on 1/26/2017.
 */

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.services.sms.IncomingSms;


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
    public static final String textToSpeak = "textToSpeak";
    public static boolean phoneFound = false;

    @Override
    protected void onHandleIntent(Intent intent) {


        String whatToSay = intent.getStringExtra(textToSpeak);
        String orignator = intent.getStringExtra(MainActivity.orignationActivityName);
        if(null == whatToSay){
            whatToSay = "Hello";
        }


        sayString(whatToSay);
        if(IncomingSms.findMyPhone.equals(orignator)){
            phoneFound = false;
            Intent dialogIntent = new Intent(this, FindMyPhoneDialog.class);
            dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);
            while(!phoneFound){

                Log.e(fileName, " phoneFound  "+phoneFound );
                sayString(whatToSay);
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


    }

    @Override
    public void onDestroy() {

    super.onDestroy();

}



}
