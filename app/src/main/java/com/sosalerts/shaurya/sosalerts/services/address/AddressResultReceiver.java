package com.sosalerts.shaurya.sosalerts.services.address;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.telephony.SmsManager;
import android.util.Log;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.powerbutton.ScreenReceiver;
import com.sosalerts.shaurya.sosalerts.services.sms.IncomingSms;
import com.sosalerts.shaurya.sosalerts.services.util.GetLocationCordinatesService;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by shaurya on 1/24/2017.
 */

public class AddressResultReceiver extends ResultReceiver {
    private static final String fileName = AddressResultReceiver.class.getSimpleName();

    private Receiver mReceiver;

    public AddressResultReceiver(Handler handler) {
        super(handler);
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);

    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }


    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        String origination = resultData.getString(MainActivity.orignationActivityName);
        Log.e(fileName, "origination "+origination+" sms ="+resultData.getString(IncomingSms.phoneNo) );

        SmsManager smsManager = SmsManager.getDefault();
        String utteranceId=this.hashCode() + "";
        String cordinates = resultData.getString(FetchAddressIntentService.LOCATION_DATA_CORDINATES);
        String country  = resultData.getString(FetchAddressIntentService.LOCATION_DATA_EXTRA_COUNTRY);
        String address = resultData.getString(FetchAddressIntentService.Location_ADDRESS);
        if ("IN".equals(country)){
            Log.e(fileName, "India " );
            cordinates = "https://maps.mapmyindia.com/@"+cordinates;
        }else {
            Log.e(fileName, "Non India " );
            cordinates = "https://www.google.com/maps/place/@"+cordinates+",16z";
        }



        String myemergencyContacts =  resultData.getString(GetLocationCordinatesService.myemergencyContactsNumbers);
        Log.e(fileName, "myemergencyContacts  ="+myemergencyContacts);
        StringTokenizer tokenizer = new StringTokenizer(myemergencyContacts, ",") ;
        List<String> myemergencyContactsList = new ArrayList<String>();
         while(tokenizer.hasMoreTokens()){
             String number = tokenizer.nextToken();
             if(null != number && number.trim().length() > 0){
                 myemergencyContactsList.add(number);
             }

        }
        Log.e(fileName, "Total myemergencyContactsList   ="+myemergencyContactsList);
        if (IncomingSms.whereAreYou.equals(origination)){
            String phoneNo = resultData.getString(IncomingSms.phoneNo);
            Log.e(fileName, "Phone no "+phoneNo);

            if(myemergencyContacts.contains(Storage.getOnlyNumbers(phoneNo))){

                if(!MainActivity.testMode){
                    Log.e(fileName, "Reply to where are you message "+"I am at "+address + " Exact location: " +cordinates);
                    smsManager.sendTextMessage(phoneNo, null,  "I am at "+address + " Exact location: " +cordinates, null, null);
                }else {
                    Log.e(fileName, "Test mode: I know you "+"I am at "+address + " Exact location: " +cordinates);
                }


            }else {
                Log.e(fileName, "I don't know you "+phoneNo);
            }

        }else if(ScreenReceiver.SOSAlert.equals(origination)){

            smsToAll(myemergencyContactsList, address, cordinates);
        }
        if (mReceiver != null) { //This can send data to activity
            Log.e(fileName, origination+" Result from result receiverrrrrrrrrrrrrrrrrrrrrrrr "+resultData.getString(FetchAddressIntentService.Location_ADDRESS));

            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }

    public static class SmsTask implements Runnable{
        SmsManager smsManager = SmsManager.getDefault();
        List<String> myemergencyContactsList;
        String msg;
        public SmsTask( List<String> myemergencyContactsList,String msg ){
            this.myemergencyContactsList = myemergencyContactsList;
            this.msg = msg;
        }
        @Override
        public void run() {
            while(MainActivity.sosAlertOnFire){
                for (String contact:myemergencyContactsList ){
                    if(!MainActivity.testMode){
                        Log.e(fileName, contact+msg );
                        smsManager.sendTextMessage(contact, null,  msg, null, null);

                    }else {
                        Log.e(fileName, "Test mode: " +msg);
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(1000 *60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private static void sendSMSinBackgreound(final Runnable runnable){
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        //return t;
    }
    public static void smsToAll(List<String> myemergencyContactsList, String address, String cordinates){
        String msg = " I am in danger.";
        if (address != null){
            msg += " I am at "+address;
        }
        if (null != cordinates){
            msg += " My Exact location: " +cordinates;
        }

        MainActivity.sosAlertOnFire = true;
        SmsTask smsTask = new SmsTask(myemergencyContactsList, msg);
        sendSMSinBackgreound(smsTask);
    }
}

