package com.sosalerts.shaurya.sosalerts.db;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.R;
import com.sosalerts.shaurya.sosalerts.services.locationTracker.LocationTrackerIntentService;
import com.sosalerts.shaurya.sosalerts.tabs.ContactsTab;
import com.sosalerts.shaurya.sosalerts.tabs.LocationsTab;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by shaurya on 1/24/2017.
 */

public class Storage {
    public static final String mostRecentExitOrEnterTime = "mostRecentExitOrEnterTime";
    public static final String averageStayAtLocation = "averageStayAtLocation_";
    public static final String batteryLevel = "batteryLevel";
    public static final String savedLocations = "SavedLocations";
    public static final String speakLocation = "speakLocation";
    public static final String useAndroidLocation = "useAndroidLocation";
    public static final String lastKnownLocationName = "lastKnownLocationName";
    public static final String lastKnownLocation = "lastKnownLocation";
    public static final String lastKnownLocationTime = "lastKnownLocationTime";
    public static final String lastKnownLocationAddress = "lastKnownLocationAddress";
    public static final String lastKnownLocationDistance = "lastKnownLocationDistance";
    public static final String currentAction = "currentAction";
    public static final String savedContacts = "savedContacts";
    public static final String settingsLocationAutoUpdates = "settingsLocationAutoUpdates";
    public static final String settingsreplyToWhereAreYou = "settingsreplyToWhereAreYou";
    public static final String replyToFindMyPhone = "replyToFindMyPhone";

    public static final String settingsSafeZoneBoundry = "settingsSafeZoneBoundry";
    public static final String settingsLocationTrackerFrequency = "settingsLocationTrackerFrequency";
    public static final String settingsPowerButtonCount = "settingsPowerButtonCount";
    public static final String countryCodeLocation  = "countryCodeLocation";
    private static final String dbName = "activity.getStringR.string.saved_location_db";
    public static  double settingsSafeZoneBoundryDefault = 500;
    public static final int settingsPowerButtonCountDefault = 7;
    public static  final String settingsLocationTrackerFrequencyDefault = "Week days";
    public static final String smartbatteryMode = "smartbatteryMode";


    private static final String fileName = Storage.class.getSimpleName();

    public static void storeinDB(String itemName, String itemValue, FragmentActivity activity){
        SharedPreferences sharedPref = activity.getSharedPreferences(dbName+itemName,activity.MODE_PRIVATE);
        store(itemName, itemValue, sharedPref);
    }
    public static void storeinDB(String itemName, String itemValue, Context activity){
        SharedPreferences sharedPref = activity.getSharedPreferences(dbName+itemName,activity.MODE_PRIVATE);
        store(itemName, itemValue, sharedPref);
    }
    private static void store(String itemName, String itemValue, SharedPreferences sharedPref){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.putString(itemName,itemValue);
        editor.commit();
    }
    public static String getFromDB(String itemName,  Context activity){
        SharedPreferences sharedPref = activity.getSharedPreferences(dbName+itemName,activity.MODE_PRIVATE);
        return sharedPref.getString(itemName, null);
    }
    public static void storeinDBStringSet(String itemName, String itemValue, Context activity){

        SharedPreferences sharedPref = activity.getSharedPreferences(dbName+itemName,activity.MODE_PRIVATE);

        Set<String> savedLocation =  sharedPref.getStringSet(itemName, null);
        if (null == savedLocation){
            savedLocation = new HashSet<String>();
        }
        savedLocation.add(itemValue);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.putStringSet(itemName,savedLocation);
        editor.commit();

        if(savedContacts.equals(itemName)){
            refreshTab(activity, ContactsTab.actionName);
        }else {
            refreshTab(activity, LocationsTab.actionName);
        }

    }

    private static void refreshTab(Context activity, String tabName){
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra(MainActivity.orignationActivityName, tabName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);

    }
    public static void deleteinDBStringSet(String itemName, String itemValue, Context activity){

        SharedPreferences sharedPref = activity.getSharedPreferences(dbName+itemName,activity.MODE_PRIVATE);

        Set<String> savedLocation =  sharedPref.getStringSet(itemName, null);
        if (null != savedLocation){
            savedLocation.remove(itemValue);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.putStringSet(itemName,savedLocation);
            editor.commit();
            Toast.makeText(activity,"Deleted!" , Toast.LENGTH_LONG)
                    .show();
            if(savedContacts.equals(itemName)){
                refreshTab(activity, ContactsTab.actionName);
            }else {
                refreshTab(activity, LocationsTab.actionName);
            }

        }

    }
    public static Set<String> getFromDBDBStringSet(String itemName,  Context activity){
        SharedPreferences sharedPref = activity.getSharedPreferences(dbName+itemName,activity.MODE_PRIVATE);
        return  sharedPref.getStringSet(itemName, null);

    }
    public static String getEmergencyContacts(Context activity){
        StringBuffer myemergencyContacts = new StringBuffer();
        Set<String> savedContacts = Storage.getFromDBDBStringSet(Storage.savedContacts,activity);

        if (savedContacts != null && savedContacts.size() > 0) {
            for (String aPhoneNo : savedContacts) {
                Log.e(fileName, "aPhoneNo = "+aPhoneNo);
                myemergencyContacts.append(","+Storage.getOnlyNumbers(aPhoneNo));
            }
        }
        Log.e(fileName, "All phones = "+myemergencyContacts.toString());
        return myemergencyContacts.toString();
    }
    public static List<String> getEmergencyContactsList(Context activity){
       String myemergencyContacts = getEmergencyContacts(activity);
        StringTokenizer tokenizer = new StringTokenizer(myemergencyContacts, ",") ;
        List<String> myemergencyContactsList = new ArrayList<String>();
        while(tokenizer.hasMoreTokens()){
            String number = tokenizer.nextToken();
            if(null != number && number.trim().length() > 0){
                myemergencyContactsList.add(number);
            }

        }
        return myemergencyContactsList;
    }

    public static String getOnlyNumbers(String aPhoneNo){
        if(null != aPhoneNo){
            aPhoneNo = aPhoneNo.replaceAll("[^\\d+]", "").trim();
            if (aPhoneNo.length() == 10){
               aPhoneNo = "0"+aPhoneNo;
            }
        }

        return aPhoneNo;
    }
    public static String getOnlyNumbersLastTen(String aPhoneNo){
        if(null != aPhoneNo){
            aPhoneNo = aPhoneNo.replaceAll("[^\\d]", "").trim();
            if (aPhoneNo.length() > 10){
                int extra = aPhoneNo.length() -10;
                aPhoneNo = aPhoneNo.substring(extra);
            }
        }

        return aPhoneNo;
    }

    public static boolean isLocationTrackerOn( Context activity){
        Date today = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(today);

        String recentEnterExitTime =  Storage.getFromDB(Storage.mostRecentExitOrEnterTime,activity);
        if(null != recentEnterExitTime && !"".equals(recentEnterExitTime.trim())){
            long recentEnterExit_Time = Long.parseLong(recentEnterExitTime);
            if((today.getTime()- recentEnterExit_Time) < 1000*60*10){//Switch of location tracking for 10 minutes after we enter or exit from known location
                return false;
            }
        }


        if (Boolean.parseBoolean(Storage.getFromDB(Storage.smartbatteryMode,activity))){
            Log.e(fileName, "Smart battery saver is on");
            Calendar trackingOffUntil = Calendar.getInstance();
            trackingOffUntil.setTimeInMillis(LocationTrackerIntentService.donotTrackUntillTime);
            if(now.before(trackingOffUntil)){
                return false;
            }
        }else{
            Log.e(fileName, "Smart battery saver is off");
        }


        String batteryLevelStr = getFromDB(Storage.batteryLevel,  activity);
        float batteryLevel = 100;
        try{
            batteryLevel = Float.parseFloat(batteryLevelStr);
        }catch(Exception e){
            batteryLevel = 100;
        }
        if(batteryLevel < 20) {
            return false;
        }
        String frequency = getFromDB(settingsLocationTrackerFrequency,activity);
        if ( "off".equalsIgnoreCase(frequency)){
            return false;
        }else {
            if ("Week days".equalsIgnoreCase(frequency)){

                if (now.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || now.get(Calendar.DAY_OF_WEEK)== Calendar.SUNDAY){
                    return false;
                }else {
                    return true;
                }
            }
            return true;
        }
    }
    public static String storeOrGetCountryCode(Context activity, String countryCode){
       SharedPreferences sharedPref = activity.getSharedPreferences(dbName+countryCodeLocation,activity.MODE_PRIVATE);
        if(null != countryCode){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.putString(countryCodeLocation,countryCode);
            editor.commit();
            return null;
        }else {
            return sharedPref.getString(countryCodeLocation, "US");
        }
    }
}
