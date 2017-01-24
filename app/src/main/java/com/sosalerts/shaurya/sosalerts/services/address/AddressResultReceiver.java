package com.sosalerts.shaurya.sosalerts.services.address;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.telephony.SmsManager;
import android.util.Log;

import com.sosalerts.shaurya.sosalerts.db.Storage;

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
        Log.e("LOB", "Result from result receiverrrrrrrrrrrrrrrrrrrrrrrr cool ="+resultData.getString(FetchAddressIntentService.LOCATION_DATA_EXTRA_COUNTRY));
        Log.e("LOB", "Result from result receiverrrrrrrrrrrrrrrrrrrrrrrr cool ="+resultData.getString(FetchAddressIntentService.LOCATION_DATA_EXTRA));
        SmsManager smsManager = SmsManager.getDefault();
        String utteranceId=this.hashCode() + "";

        /*if ("IN".equals(country)){
            Log.e("LOB", "India " );
            location = "https://maps.mapmyindia.com/@"+location;
        }else {
            Log.e("LOB", "Non India " );
            location = "https://www.google.com/maps/place/@"+location+",16z";
        }
        String parentState = Storage.getFromDB(Storage.currentAction,context);
        if("SaveLocation".equals(parentState)){
            String locationName = Storage.getFromDB(Storage.locationName,context );
            Storage.storeinDBStringSet(Storage.savedLocations,locationName+"_"+location,context);
            Log.e("LOB", "::::Saved locations :: "+Storage.getFromDBDBStringSet(Storage.savedLocations,context) );
        }*/

        //ttobj.speak("Hello", TextToSpeech.QUEUE_FLUSH, null,utteranceId);
        //smsManager.sendTextMessage("540", null, "I need help. I am at "+address + " Exact location: " +location, null, null);

        // b.putString(LOCATION_DATA_EXTRA,cordinates);
        //b.putString(RESULT_DATA_KEY,message);
        if (mReceiver != null) { //This can send data to activity
            Log.e("LOB", "Result from result receiverrrrrrrrrrrrrrrrrrrrrrrr "+resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY));

            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
