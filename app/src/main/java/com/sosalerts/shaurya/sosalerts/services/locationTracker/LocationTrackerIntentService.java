package com.sosalerts.shaurya.sosalerts.services.locationTracker;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.telephony.SmsManager;
import android.util.Log;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.util.ReadOut;
import com.sosalerts.shaurya.sosalerts.services.util.GetLocationCordinatesService;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by shaurya on 1/28/2017.
 */

public class LocationTrackerIntentService extends IntentService {
    private static String previousLocation = null;
    private static String currentLocation = null;
    private final String unknownLocation = "Unknown";

    private final String fileName = this.getClass().getSimpleName();
    public LocationTrackerIntentService(){
        super("LocationTrackerIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        double safeZoneBoundrySettings = Storage.settingsSafeZoneBoundryDefault;
        try{
            safeZoneBoundrySettings = Double.parseDouble(Storage.getFromDB(Storage.settingsSafeZoneBoundry,this));
        }catch (Exception e){
            safeZoneBoundrySettings = Storage.settingsSafeZoneBoundryDefault;
        }
        Location mLastLocation = intent.getParcelableExtra(GetLocationCordinatesService.LOCATION_CORDINATES);
        double latitude = mLastLocation.getLatitude();
        double longitude = mLastLocation.getLongitude();
        Log.e(fileName, "location In location tracker ::::: "+latitude+ " --- "+longitude);
        String location = latitude+","+longitude;
        Set<String> savedLocation = Storage.getFromDBDBStringSet(Storage.savedLocations,this);
        if (savedLocation != null && savedLocation.size() > 0) {

            for (String alocation : savedLocation) {
                int seperatorLocation = alocation.indexOf("_");
                String locationName = alocation.substring(0, seperatorLocation);
                String link = alocation.substring(seperatorLocation + 1);
                link = link.substring(link.indexOf("@")+1);

                StringTokenizer tokenizer = new StringTokenizer(link, ",") ;
                String[] locations = new String[3];
                int i=0;
                while(tokenizer.hasMoreTokens()){
                    locations[i]= tokenizer.nextToken();
                    i++;

                }
                double distance = distFrom(latitude,longitude,Double.parseDouble(locations[0]), Double.parseDouble(locations[1]));

                Storage.storeinDB(Storage.lastKnownLocationDistance, " Dis: "+distance+" accuracy "+mLastLocation.getAccuracy(),this);
                String distanceStr = ""+distance;
                if(distanceStr != null && distanceStr.length() > 6){
                    distanceStr = distanceStr.substring(0,5);
                }
                if (Boolean.parseBoolean(Storage.getFromDB(Storage.useAndroidLocation, this))) {
                    Intent speakIntent = new Intent(this, ReadOut.class);
                    speakIntent.putExtra(ReadOut.textToSpeak,distanceStr);//+" accuracy "+mLastLocation.getAccuracy()
                    speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
                    startService(speakIntent);
                }


                if (distance <safeZoneBoundrySettings){
                    currentLocation = locationName;
                    Log.e(fileName, "In safe location : "+distance+ " safeZoneBoundrySettings "+safeZoneBoundrySettings+ " locationName "+locationName);
                    break;
                }else {
                   currentLocation = unknownLocation;
                }
            }

            Log.e(fileName, "currentLocation  "+currentLocation +" previousLocation "+previousLocation);
            if(previousLocation != null){// So that the when app starts for the firs time we can ignore this code
                if(unknownLocation.equals(previousLocation) && !unknownLocation.equals(currentLocation)){
                    Intent speakIntent = new Intent(this, ReadOut.class);
                    speakIntent.putExtra(ReadOut.textToSpeak,"Entering "+currentLocation);
                    speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
                    startService(speakIntent);
                    sendSMSToAll("Entering "+currentLocation);
                }else if(!unknownLocation.equals(previousLocation) && unknownLocation.equals(currentLocation)){
                    Intent speakIntent = new Intent(this, ReadOut.class);
                    speakIntent.putExtra(ReadOut.textToSpeak,"Exiting "+previousLocation);
                    speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
                    startService(speakIntent);
                    sendSMSToAll("Exiting "+previousLocation);
                }
            }
            previousLocation = currentLocation;
        }
    }

    private void sendSMSToAll(String text){
        SmsManager smsManager = SmsManager.getDefault();
        List<String> emergencyPhones = Storage.getEmergencyContactsList(getApplicationContext());
        if (null != emergencyPhones){
            for (String phoneNo : emergencyPhones){
                smsManager.sendTextMessage(phoneNo, null,  text, null, null);
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

}
