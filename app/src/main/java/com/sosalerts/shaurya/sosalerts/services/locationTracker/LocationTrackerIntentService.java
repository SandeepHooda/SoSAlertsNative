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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by shaurya on 1/28/2017.
 */

public class LocationTrackerIntentService extends IntentService {
    public static String previousLocation = null;
    private static String currentLocation = null;
    public static final String unknownLocation = "Unknown";
    private static Date knownLocationTimeEvent = null;
    public static long donotTrackUntillTime = 0;
    private static Map<String, int[]> locationTrackerMap = new HashMap<String, int[]>();
    //private static int locationChangeEventCounts = 0;
    //private static boolean speakLocation;

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
        String locationLink = "";
        double distance = 0;
        if (savedLocation != null && savedLocation.size() > 0) {

            for (String alocation : savedLocation) {
                int seperatorLocation = alocation.indexOf("_");
                String locationName = alocation.substring(0, seperatorLocation);
                String link = alocation.substring(seperatorLocation + 1);
                locationLink = link;
                link = link.substring(link.indexOf("@")+1);

                StringTokenizer tokenizer = new StringTokenizer(link, ",") ;
                String[] locations = new String[3];
                int i=0;
                while(tokenizer.hasMoreTokens()){
                    locations[i]= tokenizer.nextToken();
                    i++;

                }
                distance = distFrom(latitude,longitude,Double.parseDouble(locations[0]), Double.parseDouble(locations[1]));


                String distanceStr = ""+distance;
                if(distanceStr.indexOf(".") >= 0){
                    distanceStr = distanceStr.substring(0,distanceStr.indexOf("."));
                }
                if (Boolean.parseBoolean(Storage.getFromDB(Storage.speakLocation, this))) {
                    Intent speakIntent = new Intent(this, ReadOut.class);
                    speakIntent.putExtra(ReadOut.textToSpeak,distanceStr+" meters from "+locationName +" calculated via "+intent.getStringExtra(GetLocationCordinatesService.LOCATION_CORDINATES_SOURCE));//+" accuracy "+mLastLocation.getAccuracy()
                    speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
                    startService(speakIntent);
                }

                double safeRadius = safeZoneBoundrySettings;
                if(previousLocation != null){
                    if(unknownLocation.equals(previousLocation)){//unknown location
                        safeRadius = safeZoneBoundrySettings + 300; //whiling entering anounce the event a bit earlier
                    }else{//known location
                        safeRadius = safeZoneBoundrySettings+ (mLastLocation.getAccuracy()*1.5);
                    }
                }

                if(!unknownLocation.equals(locationName) && distanceStr != null && !distanceStr.trim().equals("")){
                    int[] distanceArray = locationTrackerMap.get(locationName);
                    if (null == distanceArray){
                        distanceArray = new int[3];
                        try{
                            distanceArray[0] = Integer.parseInt(distanceStr.trim());
                        }catch(Exception e){

                        }

                    }else {
                        distanceArray[2]= distanceArray[1];
                        distanceArray[1]= distanceArray[0];
                        try{
                            distanceArray[0] = Integer.parseInt(distanceStr.trim());
                        }catch(Exception e){

                        }


                    }
                    locationTrackerMap.put(locationName,distanceArray);
                }

                if (distance < safeRadius){
                    currentLocation = locationName;
                    Log.e(fileName, intent.getStringExtra(GetLocationCordinatesService.LOCATION_CORDINATES_SOURCE )+" : In safe location : "+distance+ " safeZoneBoundrySettings "+safeZoneBoundrySettings+ " locationName "+locationName);
                    break;
                }else {
                   currentLocation = unknownLocation;
                }
            }

            if (previousLocation == null){//If app crashes and comes up again use the last saved location
                previousLocation = Storage.getFromDB(Storage.lastKnownLocationName,getApplicationContext());
            }
            Storage.storeinDB(Storage.lastKnownLocationName,currentLocation,getApplicationContext());
            Log.e(fileName, "currentLocation  "+currentLocation +" previousLocation "+previousLocation);
            if ( Boolean.parseBoolean(Storage.getFromDB(Storage.speakLocation, this))) {
                int[] distanceArray = locationTrackerMap.get(currentLocation);
                if (null == distanceArray){
                    distanceArray = new int[3];
                }
                Intent speakIntent = new Intent(this, ReadOut.class);
                speakIntent.putExtra(ReadOut.textToSpeak,"Previous location "+previousLocation +" current location "+currentLocation+" Accuracy "+mLastLocation.getAccuracy()
                        +" calculated via "+intent.getStringExtra(GetLocationCordinatesService.LOCATION_CORDINATES_SOURCE)+" speed "+mLastLocation.getSpeed()
                        +" distance  "+distanceArray[0]+" meters ,  "+distanceArray[1]+" meters ,  "+distanceArray[2]+" meters ");
                speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
                startService(speakIntent);
            }
            boolean changeOfLocation = false;
            //speakLocation = false;
            if(previousLocation != null && Storage.isLocationTrackerOn(getApplicationContext())){// So that the when app starts for the first time after install we can ignore this code
                //if(null == knownLocationTimeEvent || ((new Date().getTime() - knownLocationTimeEvent.getTime()) > 5*60*1000)){// No event for next five minute
                    if(unknownLocation.equals(previousLocation) && !unknownLocation.equals(currentLocation)){
                        changeOfLocation = true;
                            //speakLocation = true;
                            Storage.storeinDB(Storage.mostRecentExitOrEnterTime,""+(new Date().getTime()),getApplicationContext());
                            long entryTime = markEntryToLocationInDB(currentLocation);

                            knownLocationTimeEvent = new Date();
                            Intent speakIntent = new Intent(this, ReadOut.class);
                            speakIntent.putExtra(ReadOut.textToSpeak,"Entering "+currentLocation+" Accuracy "+mLastLocation.getAccuracy() +
                                    " calculated via "+intent.getStringExtra(GetLocationCordinatesService.LOCATION_CORDINATES_SOURCE)+" speed "+mLastLocation.getSpeed());
                            speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
                            startService(speakIntent);
                            sendSMSToAll("Entering "+currentLocation+" "+locationLink );


                    }else if(!currentLocation.equals(previousLocation) && !unknownLocation.equals(currentLocation)){ //case when directly reaches from home to infy :)
                        //if(!unknownLocation.equals(previousLocation) && unknownLocation.equals(currentLocation)){

                        if (mLastLocation.getSpeed() > 0 ){
                            int[] distanceArray = locationTrackerMap.get(currentLocation);
                            if (null == distanceArray){
                                distanceArray = new int[3];
                            }
                            if ((distanceArray[0] > distanceArray[1] ) &&( distanceArray[1] > distanceArray[2])){
                                changeOfLocation = true;
                                currentLocation = unknownLocation;  //to handle the case when directly reaches from home to infy :)
                                //speakLocation = true;
                                //Storage.storeinDB(Storage.mostRecentExitOrEnterTime,""+(new Date().getTime()),getApplicationContext());
                                long averageTime  = markExitFromLocationInDB(previousLocation);
                                knownLocationTimeEvent = new Date();
                                Intent speakIntent = new Intent(this, ReadOut.class);
                                speakIntent.putExtra(ReadOut.textToSpeak,"Exiting "+previousLocation +" Accuracy "+mLastLocation.getAccuracy() +
                                        " calculated via "+intent.getStringExtra(GetLocationCordinatesService.LOCATION_CORDINATES_SOURCE)+ " speed "+mLastLocation.getSpeed());

                                speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
                                startService(speakIntent);
                                sendSMSToAll("Exiting "+previousLocation+" "+locationLink);
                            }

                        }



                    }
                //}

            }
            if (changeOfLocation ){
                previousLocation = currentLocation;
            }

        }
    }
    private long markEntryToLocationInDB(String location){
        Calendar cal = Calendar.getInstance();
        Date today = new Date();
        cal.setTime(today);
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
          return -100;
        }
        try{
            //1. Extract the tracker vo
            String locationTrackerStr = Storage.getFromDB(Storage.averageStayAtLocation+"_"+location,getApplicationContext());
            //2. Create a location tracker and add details frm db to this object
            LocationTrackerVO locationTrackerVO = new LocationTrackerVO();
            locationTrackerVO.fillInDetailsFromLocationStr(locationTrackerStr, locationTrackerVO);
            //3. Record this entry time to the VO
            locationTrackerVO.setEntryTime(new Date().getTime());
            //4. Store back that Vo to DB
            Storage.storeinDB(Storage.averageStayAtLocation+"_"+location,locationTrackerVO.toString(),getApplicationContext());
            //5 If average stay is known then set that time
            int avegargeStay = (int) locationTrackerVO.getAverageStayInMin();
            avegargeStay = avegargeStay - (1000 * 60*60);//minus 2 hour
            cal.add(Calendar.MINUTE,avegargeStay );
            donotTrackUntillTime = cal.getTimeInMillis();
            return locationTrackerVO.getEntryTime();
        }catch(Exception e){

        }
       return -1;
    }

    private long markExitFromLocationInDB(String location){
        Calendar cal = Calendar.getInstance();
        Date today = new Date();
        cal.setTime(today);
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            return -100;
        }
        try{
            //1. Extract the tracker vo
            String locationTrackerStr = Storage.getFromDB(Storage.averageStayAtLocation+"_"+location,getApplicationContext());
            //2. Create a location tracker and add details frm db to this object
            LocationTrackerVO locationTrackerVO = new LocationTrackerVO();
            locationTrackerVO.fillInDetailsFromLocationStr(locationTrackerStr, locationTrackerVO);
            //3. Calculate average time
            locationTrackerVO.calculateAndSetAverageMinutesAtLocation(locationTrackerVO);
            //4. Store back that Vo to DB
            Storage.storeinDB(Storage.averageStayAtLocation+"_"+location,locationTrackerVO.toString(),getApplicationContext());
            return (locationTrackerVO.getAverageStayInMin() /60);
        }catch(Exception e){

        }
        return -1;
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
