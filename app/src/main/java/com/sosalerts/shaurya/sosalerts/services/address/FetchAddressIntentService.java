package com.sosalerts.shaurya.sosalerts.services.address;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.sms.IncomingSms;
import com.sosalerts.shaurya.sosalerts.services.util.GetLocationCordinatesService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by shaurya on 1/21/2017.
 */

public class FetchAddressIntentService extends IntentService{

    private final String fileName = this.getClass().getSimpleName();
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME =
                "com.google.android.gms.location.sample.locationaddress";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String Location_ADDRESS = PACKAGE_NAME +
                ".Location_ADDRESS";
        public static final String LOCATION_DATA_CORDINATES = PACKAGE_NAME +
                ".LOCATION_DATA_CORDINATES";
    public static final String LOCATION_DATA_EXTRA_COUNTRY = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA_COUNTRY";
    public static final String ADDRESS_RESULT_RECEIVER = PACKAGE_NAME +
            ".addressResultReceiver";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FetchAddressIntentService(String name) {
        super(name);
    }
    public FetchAddressIntentService() {
        super("Sandeep");
    }
    private void deliverResultToReceiver(Intent intent,int resultCode, String address, String cordinates, String countryCode, String intentOriginator, String phoneNo ,String emergencyContacts) {


        ResultReceiver rec = intent.getParcelableExtra(ADDRESS_RESULT_RECEIVER);
        Bundle b= new Bundle();
        b.putString(LOCATION_DATA_EXTRA_COUNTRY,countryCode);
        b.putString(LOCATION_DATA_CORDINATES,cordinates);
        b.putString(Location_ADDRESS,address);
        b.putString(IncomingSms.phoneNo,phoneNo);
        b.putString(MainActivity.orignationActivityName,intentOriginator);
        b.putString(GetLocationCordinatesService.myemergencyContactsNumbers,emergencyContacts);
        rec.send(0, b);
        vibrate();
        //stopSelf();
    }

    private void vibrate(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        String errorMessage = "";
        String emergencyContacts = Storage.getEmergencyContacts(getApplicationContext());

        // Get the location passed to this service through an extra.
        Location cordinates = intent.getParcelableExtra( LOCATION_DATA_CORDINATES);
        String originator = intent.getStringExtra(MainActivity.orignationActivityName);
        String phonmeNo = intent.getStringExtra(IncomingSms.phoneNo);
        List<Address> addresses = null;
        String cordinatesStr = "Cordinates Not availabe";
        if(null != cordinates){
            cordinatesStr = cordinates.getLatitude() +","+cordinates.getLongitude();
            Log.e(fileName, "Address service" + cordinatesStr +" ---"+ originator +" --"+ phonmeNo);
            try {
                addresses = geocoder.getFromLocation(
                        cordinates.getLatitude(),
                        cordinates.getLongitude(),

                        1);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                //errorMessage = getString(R.string.service_not_available);
                Log.e(fileName, "service_not_available", ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                //errorMessage = getString(R.string.);
                Log.e(fileName, "invalid_lat_long_used" + ". " +
                        "Latitude = " + cordinates.getLatitude() +
                        ", Longitude = " +
                        cordinates.getLongitude(), illegalArgumentException);
            }
        }



        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                //errorMessage = getString(R.string.no_address_found);
                Log.e(fileName, "no_address_found");
            }
            deliverResultToReceiver(intent,FAILURE_RESULT, cordinatesStr,cordinatesStr, "",originator,phonmeNo,emergencyContacts);
        } else {
            Address address = addresses.get(0);

            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));

            }
            Storage.storeOrGetCountryCode(getApplicationContext(),address.getCountryCode());
            String addrerss =  TextUtils.join(System.getProperty("line.separator"),  addressFragments);
            Storage.storeinDB(Storage.lastKnownLocationAddress, addrerss,getApplicationContext());
            deliverResultToReceiver(intent,SUCCESS_RESULT,addrerss  ,""+cordinatesStr, address.getCountryCode(),originator,phonmeNo,emergencyContacts);
        }
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
}
