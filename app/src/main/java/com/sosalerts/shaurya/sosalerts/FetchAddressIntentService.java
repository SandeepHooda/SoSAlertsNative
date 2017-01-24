package com.sosalerts.shaurya.sosalerts;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by shaurya on 1/21/2017.
 */

public class FetchAddressIntentService extends IntentService{


        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME =
                "com.google.android.gms.location.sample.locationaddress";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
    public static final String LOCATION_DATA_EXTRA_COUNTRY = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA_COUNTRY";

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
    private void deliverResultToReceiver(int resultCode, String message, String cordinates, String countryCode) {
        Intent localIntent =     new Intent(RECEIVER)

                        .putExtra(RESULT_DATA_KEY, message).putExtra(LOCATION_DATA_EXTRA,cordinates).putExtra(LOCATION_DATA_EXTRA_COUNTRY,countryCode);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);



       Log.e("LOB", "Result Broadcasted to listener" );
        //stopSelf();
    }
    @Override
    public void onDestroy(){
        Log.e("LOB", "Service destroyed & stopped ===========+++++++++" );
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        String errorMessage = "";

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra( LOCATION_DATA_EXTRA);
        Log.e("LOB", "Address service" + location.getLatitude() +" --- "+location.getLongitude());

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),

                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            //errorMessage = getString(R.string.service_not_available);
            Log.e("TAG", "service_not_available", ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            //errorMessage = getString(R.string.);
            Log.e("TAG", "invalid_lat_long_used" + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                //errorMessage = getString(R.string.no_address_found);
                Log.e("TAG", "no_address_found");
            }
            deliverResultToReceiver(FAILURE_RESULT, errorMessage,"", "");
        } else {
            Address address = addresses.get(0);
            Log.i("TAG", "All address "+addresses.get(0).getCountryCode());
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
                Log.i("TAG", address.getAddressLine(i));
            }
            Log.i("TAG", "getString(R.string.address_found)");
            deliverResultToReceiver(SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments),""+location.getLatitude()+","+
                    location.getLongitude(), address.getCountryCode());
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
