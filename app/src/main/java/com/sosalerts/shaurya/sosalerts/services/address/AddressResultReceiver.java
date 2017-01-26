package com.sosalerts.shaurya.sosalerts.services.address;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.telephony.SmsManager;
import android.util.Log;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.sms.IncomingSms;

/**
 * Created by shaurya on 1/24/2017.
 */

public class AddressResultReceiver extends ResultReceiver {

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
        Log.e("LOB", "origination "+origination+" sms ="+resultData.getString(IncomingSms.phoneNo) );

        SmsManager smsManager = SmsManager.getDefault();
        String utteranceId=this.hashCode() + "";
        String cordinates = resultData.getString(FetchAddressIntentService.LOCATION_DATA_CORDINATES);
        String country  = resultData.getString(FetchAddressIntentService.LOCATION_DATA_EXTRA_COUNTRY);
        String address = resultData.getString(FetchAddressIntentService.Location_ADDRESS);
        if ("IN".equals(country)){
            Log.e("LOB", "India " );
            cordinates = "https://maps.mapmyindia.com/@"+cordinates;
        }else {
            Log.e("LOB", "Non India " );
            cordinates = "https://www.google.com/maps/place/@"+cordinates+",16z";
        }


        //ttobj.speak("Hello", TextToSpeech.QUEUE_FLUSH, null,utteranceId);
        //smsManager.sendTextMessage("540", null, "I need help. I am at "+address + " Exact location: " +location, null, null);

        // b.putString(LOCATION_DATA_EXTRA,cordinates);
        //b.putString(RESULT_DATA_KEY,message);
        if (IncomingSms.whereAreYou.equals(origination)){
            String phoneNo = resultData.getString(IncomingSms.phoneNo);
            Log.e("LOB", "Phone no "+phoneNo);
            smsManager.sendTextMessage(phoneNo, null,  "I am at "+address + " Exact location: " +cordinates, null, null);
        }
        if (mReceiver != null) { //This can send data to activity
            Log.e("LOB", "Result from result receiverrrrrrrrrrrrrrrrrrrrrrrr "+resultData.getString(FetchAddressIntentService.Location_ADDRESS));

            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
