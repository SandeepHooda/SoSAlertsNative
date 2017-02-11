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
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
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

public class GetLocationCordinatesService extends IntentService  {
    // Google client to interact with Google API

    private LocationManager locManager;

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

        String nextChainOfDuty = intent.getStringExtra(ChainOfDuty);

        if (!locSearchResultReceived && nextChainOfDuty == null) {//Previous searc is not complete
            Log.e(fileName, " Previous search didn't complete yet ");
            //Chain of duty is null for locatin tracker request not for SOS alerts
            if (Boolean.parseBoolean(Storage.getFromDB(Storage.speakLocation, this))) {
                Intent speakIntent = new Intent(this, ReadOut.class);
                speakIntent.putExtra(ReadOut.textToSpeak," Previous search didn't complete yet  ");
                speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
                startService(speakIntent);
            }
            return;
        }
        if(locationManagerObjectList.size()  > 0){
            if (Boolean.parseBoolean(Storage.getFromDB(Storage.speakLocation, this))) {
                Intent speakIntent = new Intent(this, ReadOut.class);
                speakIntent.putExtra(ReadOut.textToSpeak," Disaster Previous search didn't complete yet  ");
                speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
                startService(speakIntent);
            }
        }else {
            if (Boolean.parseBoolean(Storage.getFromDB(Storage.speakLocation, this))) {
                Intent speakIntent = new Intent(this, ReadOut.class);
                speakIntent.putExtra(ReadOut.textToSpeak," No previous request starting new search. ");
                speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
                startService(speakIntent);
            }
        }
        Log.e(fileName, " No previous search ");

            clearPreviousListners();


        this.intent = intent;
        locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        getLocationWithBestProvider(nextChainOfDuty);


    }

    private void getLocationWithBestProvider(String nextChainOfDuty){
        List<String> providerChain = new ArrayList<String>();
        if (Boolean.parseBoolean(Storage.getFromDB(Storage.useAndroidLocation,this))){
            Log.e(fileName, " Using GPS first always");
            providerChain.add(LocationManager.GPS_PROVIDER);
            providerChain.add(LocationManager.NETWORK_PROVIDER);
        }else {
            if(null == LocationTrackerIntentService.previousLocation || LocationTrackerIntentService.unknownLocation.equals(LocationTrackerIntentService.previousLocation)){
                //Unknown area - Most probably out side of buiulding so user GPS then cell
                providerChain.add(LocationManager.GPS_PROVIDER);
                //providerChain.add(LocationManager.NETWORK_PROVIDER);
            }else {
                //Known area - Most probably in side of buiulding so user Cell then GPS
                //providerChain.add(LocationManager.NETWORK_PROVIDER);
                providerChain.add(LocationManager.GPS_PROVIDER);
            }
        }


        locSearchResultReceived = false;
        boolean isEmergency = (nextChainOfDuty == null) ? false:true;
        getLocationViaProvider(providerChain, isEmergency);
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
    private void getLocationViaProvider(List<String> providerChain, boolean isEmergency) {
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
           /* if(providerChain.size() > 1){
                textToSpeak+=". Now I will try with "+providerChain.get(1);
            }else {
                textToSpeak +=". Nothing more to try";
            }*/
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
        /*sleepTask = new WaitForProviderToGetResults();
        sleepTask.execute(providerChain);//Remove listner after waiting enough for result*/
        removeGpsListner(new RemoveListnerTask(providerChain));
    }


    /*private  class WaitForProviderToGetResults extends AsyncTask<List<String>, Void, List<String>>{


        @Override
        protected List<String> doInBackground(List<String>... providerChain) {
            try {
                Thread.sleep(1000*10);
            } catch (InterruptedException e) {

            }

           return providerChain[0];
        }
        protected void onPostExecute(List<String> providerChain) {
            getLocationViaProvider(providerChain, false);
        }
    }*/

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
            getLocationViaProvider(providerChain, false);
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





    private boolean isLocationRecent(long locationTime){
        return ((new Date().getTime() - locationTime)  < 1000 * 60 );
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
                if(isLocationRecent(mLastLocation.getTime())){
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


}
