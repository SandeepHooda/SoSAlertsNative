package com.sosalerts.shaurya.sosalerts.services.util;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.address.AddressResultReceiver;
import com.sosalerts.shaurya.sosalerts.services.address.SavedLocations;
import com.sosalerts.shaurya.sosalerts.services.sms.IncomingSms;
import com.sosalerts.shaurya.sosalerts.services.sms.ReadOut;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * Created by shaurya on 1/26/2017.
 */

public class GetLocationCordinatesActivity extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener , AddressResultReceiver.Receiver{
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private final String fileName = this.getClass().getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GetLocationCordinatesActivity(String name) {
        super(name);
    }
    public GetLocationCordinatesActivity() {
        super("GetLocationCordinatesActivity");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(fileName, "Service to get user location ");
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
                //googleAPI.getErrorDialog(this, resultCode,        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else {
                Toast.makeText(getApplicationContext(),     "This device is not supported.", Toast.LENGTH_LONG).show();

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
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();
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
    private void displayLocation() {
        Log.e(fileName, " display location ");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        Log.e(fileName, " mLastLocation "+mLastLocation);
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            Log.e(fileName, "location ::::: "+latitude+ " --- "+longitude);
            String location = latitude+","+longitude;

            Set<String> savedLocation = Storage.getFromDBDBStringSet(Storage.savedLocations,this);
            Log.e(fileName, "getSavedLocations  "+savedLocation);
            if (savedLocation != null && savedLocation.size() > 0) {
                List<SavedLocations> savedLocationList = new ArrayList<SavedLocations>();
                for (String alocation : savedLocation) {
                    int seperatorLocation = alocation.indexOf("_");
                    String locationName = alocation.substring(0, seperatorLocation);
                    String link = alocation.substring(seperatorLocation + 1);
                    SavedLocations aLocation = new SavedLocations();
                    aLocation.setLocationName(locationName);
                    aLocation.setLink(link);
                    savedLocationList.add(aLocation);
                    link = link.substring(link.indexOf("@")+1);

                    StringTokenizer tokenizer = new StringTokenizer(link, ",") ;
                    String[] locations = new String[3];
                    int i=0;
                    while(tokenizer.hasMoreTokens()){
                        locations[i]= tokenizer.nextToken();
                        System.out.println(locations[i]);
                        i++;

                    }
                    double distance = distFrom(latitude,longitude,Double.parseDouble(locations[0]), Double.parseDouble(locations[1]));
                    if (distance <500){
                        /*Intent speakIntent = new Intent(this, ReadOut.class);
                        speakIntent.putExtra(MainActivity.textToSpeak,locationName);
                        this.startService(speakIntent);*/
                    }
                    Log.e(fileName, "Distance  "+distance);
                }
            }


        }
    }


    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = (double) (earthRadius * c);

        return dist;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

    }


}
