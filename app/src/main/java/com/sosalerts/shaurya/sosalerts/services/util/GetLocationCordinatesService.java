package com.sosalerts.shaurya.sosalerts.services.util;


import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.ResultReceiver;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.address.AddressResultReceiver;
import com.sosalerts.shaurya.sosalerts.services.address.FetchAddressIntentService;
import com.sosalerts.shaurya.sosalerts.services.locationTracker.LocationTrackerIntentService;
import com.sosalerts.shaurya.sosalerts.services.sms.IncomingSms;
import com.sosalerts.shaurya.sosalerts.tabs.LocationsTab;

import java.util.Locale;


/**
 * Created by shaurya on 1/26/2017.
 */

public class GetLocationCordinatesService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private final String fileName = this.getClass().getSimpleName();
    private Location mLastLocation;
    public static final String PACKAGE_NAME =         "com.sosalerts.shaurya.sosalerts.services.util.GetLocationCordinatesService";
    //public static final String ADDRESS_RESULT_RECEIVER = PACKAGE_NAME +        ".addressResultReceiver";
    public static final String LOCATION_CORDINATES = PACKAGE_NAME +        ".LOCATION_CORDINATES";
    public static final String SAVED_LOCATIONS = PACKAGE_NAME +        ".SAVED_LOCATIONS";
    public static final String GetLocationCordinatesServiceReceiver = PACKAGE_NAME +        ".GetLocationCordinatesServiceReceiver";
    public static final String ChainOfDuty = PACKAGE_NAME +        ".ChainOfDuty";
    public static final String ChainOfDuty_Address =   "ChainOfDuty_Address";
    public static final String ChainOfDuty_SMS_ONENumber =   "ChainOfDuty_SMS_ONENumber";
    public static final String ChainOfDuty_SMS_AllContact =   "ChainOfDuty_SMS_AllContact";
    public static final String myemergencyContactsNumbers = "myemergencyContactsNumbers";

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
        getLocation();

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
    private void getLocation() {
        Log.e(fileName, " display location ");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        try {
            Thread.sleep(1000); //So that location is available
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        if (null != mLastLocation){
            processLocationResults();
        }else {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setNumUpdates(1);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }


    }
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        processLocationResults();
    }

    private void processLocationResults(){
        Log.e(fileName, " mLastLocation "+mLastLocation);

        String nextChainOfDuty = intent.getStringExtra(ChainOfDuty);

        if(nextChainOfDuty != null && nextChainOfDuty.indexOf(ChainOfDuty_Address) >= 0){
            if (nextChainOfDuty.indexOf(",") > 0){
                nextChainOfDuty = nextChainOfDuty.substring(nextChainOfDuty.indexOf(",")+1);
            }else {
                nextChainOfDuty = null;
            }

            Intent addressNameIntent = new Intent(this, FetchAddressIntentService.class);
            addressNameIntent.putExtra(IncomingSms.phoneNo, intent.getStringExtra(IncomingSms.phoneNo));
            addressNameIntent.putExtra(FetchAddressIntentService.LOCATION_DATA_CORDINATES,mLastLocation);
            addressNameIntent.putExtra(MainActivity.orignationActivityName,intent.getStringExtra(MainActivity.orignationActivityName));
            addressNameIntent.putExtra(ChainOfDuty,nextChainOfDuty);
            addressNameIntent.putExtra(myemergencyContactsNumbers,intent.getStringExtra(myemergencyContactsNumbers));
            addressNameIntent.putExtra(FetchAddressIntentService.ADDRESS_RESULT_RECEIVER,new AddressResultReceiver(new Handler()));
            startService(addressNameIntent);
        }else{
            if (mLastLocation != null){
                String currentLocationName = intent.getStringExtra(LocationsTab.savedLocationName);
                if (currentLocationName != null){
                    Locale locale;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        locale = getApplicationContext().getResources().getConfiguration().getLocales().get(0);
                    } else {
                        locale = getApplicationContext().getResources().getConfiguration().locale;
                    }
                    Log.e(fileName, " Storing this location in db Locale Country ="+locale.getCountry() + "  "+locale.getDisplayCountry());
                    String location = mLastLocation.getLatitude()+","+mLastLocation.getLongitude();
                    String country = Storage.storeOrGetCountryCode(this,null);
                    String mapLink = "https://www.google.com/maps/place/@";
                    if ("IN".equals(country)){
                        mapLink= "https://maps.mapmyindia.com/@";
                    }
                    Storage.storeinDBStringSet(Storage.savedLocations, currentLocationName +"_"+mapLink+location+",16z",this);
                }

                Intent addLocatorIntent = new Intent(this, LocationTrackerIntentService.class);
                addLocatorIntent.putExtra(LOCATION_CORDINATES,mLastLocation);
                startService(addLocatorIntent);
            }else {
                Log.e(fileName, " Location is null !!");
            }

        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



}
