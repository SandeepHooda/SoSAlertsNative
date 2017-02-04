package com.sosalerts.shaurya.sosalerts.services.address;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.powerbutton.ScreenReceiver;
import com.sosalerts.shaurya.sosalerts.services.sms.IncomingSms;
import com.sosalerts.shaurya.sosalerts.services.util.GetLocationCordinatesService;
import com.sosalerts.shaurya.sosalerts.services.util.MyDialog;
import com.sosalerts.shaurya.sosalerts.services.util.PhoneVibrate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by shaurya on 1/24/2017.
 */

public class AddressResultReceiver extends ResultReceiver {
    private final String fileName = this.getClass().getSimpleName();

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

            for (String contact:myemergencyContactsList ){
                if(!MainActivity.testMode){
                    Log.e(fileName, contact+": I am in danger "+contact +"I am at "+address + " Exact location: " +cordinates);
                    smsManager.sendTextMessage(contact, null,  "I am in danger. I am at "+address + " Exact location: " +cordinates, null, null);

                }else {
                    Log.e(fileName, "Test mode: I am in danger "+contact +"I am at "+address + " Exact location: " +cordinates);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (mReceiver != null) { //This can send data to activity
            Log.e(fileName, origination+" Result from result receiverrrrrrrrrrrrrrrrrrrrrrrr "+resultData.getString(FetchAddressIntentService.Location_ADDRESS));

            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
