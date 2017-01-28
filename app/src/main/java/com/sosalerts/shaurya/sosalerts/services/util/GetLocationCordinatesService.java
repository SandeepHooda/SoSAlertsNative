package com.sosalerts.shaurya.sosalerts.services.util;


import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.ResultReceiver;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.services.locationTracker.LocationTrackerIntentService;


/**
 * Created by shaurya on 1/26/2017.
 */

public class GetLocationCordinatesService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private final String fileName = this.getClass().getSimpleName();
    private Location mLastLocation;
    public static final String PACKAGE_NAME =         "com.sosalerts.shaurya.sosalerts.services.util.GetLocationCordinatesService";
    public static final String ADDRESS_RESULT_RECEIVER = PACKAGE_NAME +        ".addressResultReceiver";
    public static final String LOCATION_CORDINATES = PACKAGE_NAME +        ".LOCATION_CORDINATES";
    public static final String SAVED_LOCATIONS = PACKAGE_NAME +        ".SAVED_LOCATIONS";
    public static final String GetLocationCordinatesServiceReceiver = PACKAGE_NAME +        ".GetLocationCordinatesServiceReceiver";

    private String whatToSpeak;
    private LocationTrackerIntentService locationReceiver ;

    private Intent intent;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GetLocationCordinatesService(String name) {
        super(name);
    }
    public GetLocationCordinatesService() {
        super("GetLocationCordinatesService");
    }


    @Override
    public void onDestroy() {
        Log.e(fileName, "Service destroyed ");

    }
    @Override
    protected void onHandleIntent(Intent intent) {
        String data = intent.getStringExtra(MainActivity.orignationActivityName);
        this.intent = intent;
        userLocationFacade();

    }

    public void userLocationFacade(){
        if (checkPlayServices()) {
            Log.e(fileName, "buildGoogleApiClient");
            // Building the GoogleApi client
            buildGoogleApiClient();
            getUserLocation();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(resultCode)) {
            }
            else {
                //Toast.makeText(getApplicationContext(),     "This device is not supported.", Toast.LENGTH_LONG).show();

            }
            return false;
        }
        return true;
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        deliverResults(intent);

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }
    private void getUserLocation(){
        Log.e(fileName, " getting location ");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
    private void deliverResults(Intent intent) {
        Log.e(fileName, " display location ");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        Log.e(fileName, " mLastLocation "+mLastLocation);
        if (mLastLocation != null) {
            ResultReceiver receiver = null;
            if(null != intent){
                receiver = intent.getParcelableExtra(ADDRESS_RESULT_RECEIVER);
            }

            if(receiver != null){
                Bundle b= new Bundle();
                b.putParcelable(LOCATION_CORDINATES,mLastLocation);
                receiver.send(0, b);
            }else {
                Intent addLocatorIntent = new Intent(this, LocationTrackerIntentService.class);
                addLocatorIntent.putExtra(LOCATION_CORDINATES,mLastLocation);
                startService(addLocatorIntent);
            }


        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



}
