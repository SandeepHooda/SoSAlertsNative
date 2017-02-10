package com.sosalerts.shaurya.sosalerts.services.sms;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.address.AddressResultReceiver;
import com.sosalerts.shaurya.sosalerts.services.util.GetLocationCordinatesService;
import com.sosalerts.shaurya.sosalerts.services.util.ReadOut;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
                        message = message.replaceAll("\\?","").replaceAll("\\.","");
                    }
                    if("fmp".equals(message) || "find my phone".equals(message)){
                        if (Boolean.parseBoolean(Storage.getFromDB(Storage.replyToFindMyPhone,context))){
                            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
                            Intent speakIntent = new Intent(context, ReadOut.class);
                            speakIntent.putExtra(ReadOut.textToSpeak,"Here I am");
                            speakIntent.putExtra(MainActivity.orignationActivityName,findMyPhone);
                            context.startService(speakIntent);
                        }

                    }
                    if(Boolean.parseBoolean(Storage.getFromDB(Storage.settingsreplyToWhereAreYou,context))){
                        String myemergencyContacts = Storage.getEmergencyContacts(context);
                        StringTokenizer tokenizer = new StringTokenizer(myemergencyContacts, ",") ;
                        List<String> myemergencyContactsList = new ArrayList<String>();
                        while(tokenizer.hasMoreTokens()){
                            String number = tokenizer.nextToken();
                            if(null != number && number.trim().length() > 0){
                                myemergencyContactsList.add(Storage.getOnlyNumbersLastTen(number));
                            }

                        }
                        if("wru".equals(message) || "where are you".equals(message)){
                            if(Storage.isLocationTrackerOn(context)){//Use last known location
                                String lastKnownLocation = Storage.getFromDB(Storage.lastKnownLocation,context);
                                lastKnownLocation += " at "+Storage.getFromDB(Storage.lastKnownLocationTime,context);
                                String address = Storage.getFromDB(Storage.lastKnownLocationAddress,context);

                                AddressResultReceiver.sendSMSIfKnown(senderNum, myemergencyContactsList, address, lastKnownLocation);
                            }else {

                                Intent locationCordinatesIntent = new Intent(context, GetLocationCordinatesService.class);
                                locationCordinatesIntent.putExtra(phoneNo, senderNum);
                                String  ChainOfDuty = GetLocationCordinatesService.ChainOfDuty_Address+","+GetLocationCordinatesService.ChainOfDuty_SMS_ONENumber;
                                locationCordinatesIntent.putExtra(GetLocationCordinatesService.ChainOfDuty, ChainOfDuty);
                                locationCordinatesIntent.putExtra(MainActivity.orignationActivityName, whereAreYou);
                                locationCordinatesIntent.putExtra(GetLocationCordinatesService.myemergencyContactsNumbers,myemergencyContacts);//myemergencyContacts
                                context.startService(locationCordinatesIntent);
                            }


                        }
                    }


                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(fileName, "Exception smsReceiver ::: " +e);

        }
    }



}
