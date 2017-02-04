package com.sosalerts.shaurya.sosalerts.services.util;


import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
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
import com.sosalerts.shaurya.sosalerts.services.alarm.AlarmReceiver;
import com.sosalerts.shaurya.sosalerts.services.locationTracker.LocationTrackerIntentService;
import com.sosalerts.shaurya.sosalerts.services.sms.IncomingSms;
import com.sosalerts.shaurya.sosalerts.tabs.LocationsTab;

import java.util.Date;
import java.util.Locale;


/**
 * Created by shaurya on 1/26/2017.
 */

public class GetLocationCordinatesService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    // Google client to interact with Google API
    private static boolean useGoogleApi = false;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager locManager;
    private int locationSource = 0;
    private int getLocationSourceFusedApi = 0;
    private int getLocationSourceFusedApiUpdateListner = 1;
    private int getLocationSourceLocationManager = 2;
    private int getLocationSourceLocationManagerUpdateListner = 3;
    private android.location.LocationListener locManagerLocationListenerNetwork;
    private android.location.LocationListener locManagerLocationListenerGPS;
    private final String fileName = this.getClass().getSimpleName();
    private Location mLastLocation;
    public static final String PACKAGE_NAME = "com.sosalerts.shaurya.sosalerts.services.util.GetLocationCordinatesService";
    //public static final String ADDRESS_RESULT_RECEIVER = PACKAGE_NAME +        ".addressResultReceiver";
    public static final String LOCATION_CORDINATES = PACKAGE_NAME + ".LOCATION_CORDINATES";
    public static final String LOCATION_CORDINATES_SOURCE = PACKAGE_NAME + ".LOCATION_CORDINATES_SOURCE";
    public static final String SAVED_LOCATIONS = PACKAGE_NAME + ".SAVED_LOCATIONS";
    public static final String GetLocationCordinatesServiceReceiver = PACKAGE_NAME + ".GetLocationCordinatesServiceReceiver";
    public static final String ChainOfDuty = PACKAGE_NAME + ".ChainOfDuty";
    public static final String ChainOfDuty_Address = "ChainOfDuty_Address";
    public static final String ChainOfDuty_SMS_ONENumber = "ChainOfDuty_SMS_ONENumber";
    public static final String ChainOfDuty_SMS_AllContact = "ChainOfDuty_SMS_AllContact";
    public static final String myemergencyContactsNumbers = "myemergencyContactsNumbers";

    private String whatToSpeak;
    private LocationTrackerIntentService locationReceiver;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;
    LocationListener[] mLocationListeners ;

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
        //powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        //wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
        //wakeLock.acquire();
        this.intent = intent;
        if (Boolean.parseBoolean(Storage.getFromDB(Storage.useAndroidLocation, this))) {
            useGoogleApi = false;
        }
        if(useGoogleApi){
            userLocationFacade();
            Log.e(fileName, "Getting location : Using google service  - BAU");
      } else {
            Log.e(fileName, "Getting location : Using google location via cellular network - New");
            getLocationViaNetwork();
        }

    }

    private android.location.LocationListener getListner(){
        return new android.location.LocationListener() {
            public void onLocationChanged(Location location) {
                Log.e(fileName, " Location listner called !!!!!!!");
                locationSource = getLocationSourceLocationManagerUpdateListner;
                mLastLocation = location;
                processLocationResults();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }
    private void getLocationViaNetwork() {
        locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        locManagerLocationListenerNetwork = getListner();
        locManagerLocationListenerGPS = getListner();

        /*Location location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(null != location){
        locationSource = getLocationSourceLocationManager;
            Log.e(fileName, " got location via network");
            mLastLocation = location;
            processLocationResults(false);
        }else{
            Log.e(fileName, " No location via network");
        }*/

        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locManagerLocationListenerNetwork);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locManagerLocationListenerGPS);
        try {
            Thread.sleep(5000);
            locManager.removeUpdates(locManagerLocationListenerGPS);
            Log.e(fileName, " Removed location listner after sleep");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void userLocationFacade() {
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
            if (googleAPI.isUserResolvableError(resultCode)) {
            } else {
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

    private void getUserLocation() {
        Log.e(fileName, " getting location ");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    private void getLocation() {
        Log.e(fileName, " display location ");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        /*try {
            Thread.sleep(1000); //So that location is available
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        mLastLocation = null;//LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (null != mLastLocation) {
            locationSource = getLocationSourceFusedApi;
            processLocationResults();
        } else {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setNumUpdates(1);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setExpirationDuration(1000);
            // mLocationRequest.setFastestInterval(500);
            // mLocationRequest.setInterval(500);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }


    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        locationSource = getLocationSourceFusedApiUpdateListner;
        processLocationResults();
    }

    private void processLocationResults() {

        String locationCordinatesSource = "";

        if (locationSource == getLocationSourceLocationManagerUpdateListner) {
            locationCordinatesSource = " via Cell";
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
               return;
            }

            locManager.removeUpdates(locManagerLocationListenerNetwork);
            Log.e(fileName, " Removed location listner in result processing");
        }

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            locationCordinatesSource = " via GPS";
            if(locationSource == getLocationSourceFusedApiUpdateListner){
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
            }
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;

        }
        Log.e(fileName, " provider = "+mLastLocation.getProvider()+"  accuracy "+mLastLocation.getAccuracy());
        String location = mLastLocation.getLatitude()+","+mLastLocation.getLongitude();
        String mapLink = "https://www.google.com/maps/place/@"+location+",16z";
        Storage.storeinDB(Storage.lastKnownLocation, mapLink,this);
        Storage.storeinDB(Storage.lastKnownLocationTime, new Date(mLastLocation.getTime()).toString(),this);
        Log.e(fileName, " mLastLocation "+location);

        String nextChainOfDuty = intent.getStringExtra(ChainOfDuty);

        if(nextChainOfDuty != null && nextChainOfDuty.indexOf(ChainOfDuty_Address) >= 0){
            if (nextChainOfDuty.indexOf(",") > 0){
                nextChainOfDuty = nextChainOfDuty.substring(nextChainOfDuty.indexOf(",")+1);
            }else {
                nextChainOfDuty = null;
            }
            Log.e(fileName," IncomingSms.phoneNo @@@@@@@@@@@@ "+intent.getStringExtra(IncomingSms.phoneNo));
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
                   Storage.storeinDBStringSet(Storage.savedLocations, currentLocationName +"_"+mapLink,this);
                }

                useGoogleApi = !useGoogleApi;
                if(mLastLocation.getAccuracy() < 100){
                    Intent addLocatorIntent = new Intent(this, LocationTrackerIntentService.class);
                    addLocatorIntent.putExtra(LOCATION_CORDINATES,mLastLocation);
                    addLocatorIntent.putExtra(LOCATION_CORDINATES_SOURCE,locationCordinatesSource);
                    startService(addLocatorIntent);
                }else {
                    if (Boolean.parseBoolean(Storage.getFromDB(Storage.speakLocation,this ))) {
                        Intent speakIntent = new Intent(this, ReadOut.class);
                        speakIntent.putExtra(ReadOut.textToSpeak,"Location accuracy too less "+mLastLocation.getAccuracy()+" meters ");//+" accuracy "+mLastLocation.getAccuracy()
                        speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
                        startService(speakIntent);
                    }
                    Log.e(fileName, " Not very accurate ");
                }

            }else {
                Log.e(fileName, " Location is null !!");
            }

        }
        //wakeLock.release();
        AlarmReceiver.completeWakefulIntent(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



}
