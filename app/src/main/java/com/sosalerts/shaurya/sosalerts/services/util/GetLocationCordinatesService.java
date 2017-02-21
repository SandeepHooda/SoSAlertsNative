package com.sosalerts.shaurya.sosalerts.services.util;


import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.address.AddressResultReceiver;
import com.sosalerts.shaurya.sosalerts.services.address.FetchAddressIntentService;
import com.sosalerts.shaurya.sosalerts.services.alarm.AlarmReceiver;
import com.sosalerts.shaurya.sosalerts.services.locationTracker.LocationTrackerIntentService;
import com.sosalerts.shaurya.sosalerts.services.sms.IncomingSms;
import com.sosalerts.shaurya.sosalerts.services.vo.LocationManagerObject;
import com.sosalerts.shaurya.sosalerts.tabs.LocationsTab;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Created by shaurya on 1/26/2017.
 */

public class GetLocationCordinatesService extends IntentService  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    // Google client to interact with Google API

    private LocationManager locManager;
    private GoogleApiClient mGoogleApiClient;
    private LocationListener locManagerLocationListener;
    private final String fileName = this.getClass().getSimpleName();
    private Location mLastLocation;
    public static final String PACKAGE_NAME = "com.sosalerts.shaurya.sosalerts.services.util.GetLocationCordinatesService";

    public static final String LOCATION_CORDINATES = PACKAGE_NAME + ".LOCATION_CORDINATES";
    public static final String LOCATION_CORDINATES_SOURCE = PACKAGE_NAME + ".LOCATION_CORDINATES_SOURCE";
    public static final String SAVED_LOCATIONS = PACKAGE_NAME + ".SAVED_LOCATIONS";
    public static final String GetLocationCordinatesServiceReceiver = PACKAGE_NAME + ".GetLocationCordinatesServiceReceiver";
    public static final String ChainOfDuty = PACKAGE_NAME + ".ChainOfDuty";
    public static final String ChainOfDuty_Address = "ChainOfDuty_Address";
    public static final String ChainOfDuty_SMS_ONENumber = "ChainOfDuty_SMS_ONENumber";
    public static final String ChainOfDuty_SMS_AllContact = "ChainOfDuty_SMS_AllContact";
    public static final String myemergencyContactsNumbers = "myemergencyContactsNumbers";
   // private WaitForProviderToGetResults sleepTask;

    private static List<LocationManagerObject> locationManagerObjectList = new ArrayList<LocationManagerObject>();

    private String whatToSpeak;


    private static boolean locSearchResultReceived = true;



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

    private float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f;
    }

    public boolean checkIfTrackingEnabled(){
        Storage.storeinDB(Storage.batteryLevel, ""+getBatteryLevel(), getApplicationContext());
        return Storage.isLocationTrackerOn(getApplicationContext());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean trackingEnabled = checkIfTrackingEnabled();
        Log.e(fileName, " Start location hunt! "+trackingEnabled);
        if(!trackingEnabled){
            return;
        }

        if (checkPlayServices()) {
            buildGoogleApiClient();
        }
        String nextChainOfDuty = intent.getStringExtra(ChainOfDuty);

        if (!locSearchResultReceived && nextChainOfDuty == null) {//Previous search is not complete and since nextChainOfDuty is null this is not emergency call
            Log.e(fileName, " Previous search didn't complete yet ");
            return;
        }

       clearPreviousListners();


        this.intent = intent;
        locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mGoogleApiClient.connect();//Try with fused location fist
    }

    private void getGPSLocation(String nextChainOfDuty){
        List<String> providerChain = new ArrayList<String>();
        providerChain.add(LocationManager.GPS_PROVIDER);
        locSearchResultReceived = false;
        boolean isEmergency = (nextChainOfDuty == null) ? false:true;
        getLocationViaProviderChain(providerChain, isEmergency);
    }
    private void getLocationViaProviderChain(List<String> providerChain, boolean isEmergency) {
        if(isEmergency){
            getLastKnownLocation();
        }
        Log.e(fileName, " chain of provider "+providerChain+" locSearchResultReceived ? "+locSearchResultReceived);

        clearPreviousListners();

        if(null == providerChain || providerChain.size() ==0 || locSearchResultReceived) {

            locSearchResultReceived = true;
            return;
        }
        String provider = providerChain.get(0);
        if (Boolean.parseBoolean(Storage.getFromDB(Storage.speakLocation, this))) {
            Intent speakIntent = new Intent(this, ReadOut.class);
            String textToSpeak = " Trying with "+provider;

            speakIntent.putExtra(ReadOut.textToSpeak,textToSpeak);
            speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
            startService(speakIntent);
        }
        Log.e(fileName, " Attempt to get location via  "+provider);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locManagerLocationListener = getListner(provider);
        LocationManagerObject obj = new LocationManagerObject();
        obj.setLocManager(locManager);
        obj.getListners().add(locManagerLocationListener);
        synchronized (this){
            locationManagerObjectList.add(obj);
        }


        providerChain.remove(0);
        locManager.requestLocationUpdates(provider, 0, 0, locManagerLocationListener);

        removeGpsListner(new RemoveListnerTask(providerChain));
    }

    private LocationListener getListner(String type){
        final String locationProvider = type;
        return new LocationListener() {
            public void onLocationChanged(Location location) {
               Log.e(fileName, locationProvider+" Location listner called !!!!!!!");
                mLastLocation = location;
                processLocationResults(locationProvider, location, false);
                //sleepTask.cancel(true);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }

    private void getLastKnownLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location= locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        processLocationResults("Network Last known", location, true);
        Location locationGPS= locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        processLocationResults("Network Last known GPS ", locationGPS, true);
    }



    private  class RemoveListnerTask implements Runnable{
      private List<String> providerChain;
       public RemoveListnerTask(List<String> providerChain){
           this.providerChain = providerChain;
       }
        @Override
        public void run() {
            try {
                Thread.sleep(1000*10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.e("RemoveListnerTask","Removed GPS listner after sleep ###");
            getLocationViaProviderChain(providerChain, false);
        }
    }
    private static void removeGpsListner(final Runnable runnable){
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

    }





    private boolean isLocationRecentAndAccurate(Location location){
        if(location.getAccuracy() > 100) return false;
        return ((new Date().getTime() - location.getTime())  < 1000 * 60 );
    }


    private void processLocationResults(String locationProvider, Location mLastLocation, boolean isEmergency) {
        String nextChainOfDuty = intent.getStringExtra(ChainOfDuty);



        Log.e(fileName, " provider = "+mLastLocation.getProvider()+"  accuracy "+mLastLocation.getAccuracy());
        String location = mLastLocation.getLatitude()+","+mLastLocation.getLongitude();
        String mapLink = "https://www.google.com/maps/place/@"+location+",16z";
        Storage.storeinDB(Storage.lastKnownLocation, mapLink,this);
        Storage.storeinDB(Storage.lastKnownLocationTime, new Date(mLastLocation.getTime()).toString(),this);
        Storage.storeinDB(Storage.lastKnownLocationAddress, "",getApplicationContext());
        Log.e(fileName, " mLastLocation "+mapLink);



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

                //Perioid Alarm to get location
                if(isLocationRecentAndAccurate(mLastLocation)){
                    String texttoSpeak = locationProvider+" Got your location with accuracy "+mLastLocation.getAccuracy();
                    Log.e(fileName,texttoSpeak);
                    if (Boolean.parseBoolean(Storage.getFromDB(Storage.speakLocation,getApplicationContext() ))) {
                        Intent speakIntent = new Intent(getApplicationContext(), ReadOut.class);
                        speakIntent.putExtra(ReadOut.textToSpeak,texttoSpeak);
                        speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
                        getApplicationContext().startService(speakIntent);
                    }
                    Intent addLocatorIntent = new Intent(this, LocationTrackerIntentService.class);
                    addLocatorIntent.putExtra(LOCATION_CORDINATES,mLastLocation);
                    addLocatorIntent.putExtra(LOCATION_CORDINATES_SOURCE,locationProvider);
                    startService(addLocatorIntent);
                }else {
                    if (Boolean.parseBoolean(Storage.getFromDB(Storage.speakLocation,this ))) {
                        Intent speakIntent = new Intent(this, ReadOut.class);
                        speakIntent.putExtra(ReadOut.textToSpeak,"Location accuracy too less  or old in time "+mLastLocation.getAccuracy()+" meters ");//+" accuracy "+mLastLocation.getAccuracy()
                        speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
                        startService(speakIntent);
                    }
                    Log.e(fileName, " Not very accurate ");
                }

            }else {
                Log.e(fileName, " Location is null !!");
            }

        }
        if(!isEmergency){
            clearPreviousListners();
            locSearchResultReceived = true;
        }

        //wakeLock.release();
        AlarmReceiver.completeWakefulIntent(intent);
    }


    @Override
    public void onDestroy() {
        Log.e(fileName, "Service destroyed ");

    }

    private void clearPreviousListners(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            return;
        }

        synchronized (this){
           if(locationManagerObjectList.size()>0){
                Log.e(fileName, "About to clear previous listners ##########");
                Iterator<LocationManagerObject> itr = locationManagerObjectList.iterator();
                while(itr.hasNext()){
                    LocationManagerObject obj = itr.next();
                    if (null != obj){
                        LocationManager mgr = obj.getLocManager();
                        if (null != mgr){
                            for(LocationListener listener: obj.getListners()){
                                mgr.removeUpdates(listener);
                                Log.e(fileName, "Cleared a listners");
                            }
                        }
                    }
                    itr.remove();
                }
                Log.e(fileName, "Cleared all listners ########");
            }

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

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        }

    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(null != mLastLocation && isLocationRecentAndAccurate(mLastLocation)){

            if(null != mGoogleApiClient && mGoogleApiClient.isConnected()){
                mGoogleApiClient.disconnect();
            }

            processLocationResults("Fused api", mLastLocation, false);
        }else {
            getGPSLocation(intent.getStringExtra(ChainOfDuty));
        }

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }
}
