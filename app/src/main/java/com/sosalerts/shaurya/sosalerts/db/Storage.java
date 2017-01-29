package com.sosalerts.shaurya.sosalerts.db;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.R;
import com.sosalerts.shaurya.sosalerts.tabs.ContactsTab;
import com.sosalerts.shaurya.sosalerts.tabs.LocationsTab;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by shaurya on 1/24/2017.
 */

public class Storage {
    public static final String savedLocations = "SavedLocations";
    public static final String currentAction = "currentAction";
    public static final String savedContacts = "savedContacts";
    public static final String settingsLocationAutoUpdates = "settingsLocationAutoUpdates";
    public static final String settingsreplyToWhereAreYou = "settingsreplyToWhereAreYou";
    public static final String settingsSafeZoneBoundry = "settingsSafeZoneBoundry";
    public static final String settingsPowerButtonCount = "settingsPowerButtonCount";
    private static final String dbName = "activity.getStringR.string.saved_location_db";
    public static final int settingsSafeZoneBoundryDefault = 200;
    public static final int settingsPowerButtonCountDefault = 5;

    public static void storeinDB(String itemName, String itemValue, FragmentActivity activity){
        SharedPreferences sharedPref = activity.getSharedPreferences(dbName+itemName,activity.MODE_PRIVATE);
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
                myemergencyContacts.append(","+Storage.getOnlyNumbers(aPhoneNo));
            }
        }
        return myemergencyContacts.toString();
    }

    public static String getOnlyNumbers(String aPhoneNo){
        if(null != aPhoneNo){
            aPhoneNo = aPhoneNo.replaceAll("[^\\d]", "").trim();
            if (aPhoneNo.length() > 10){
                int extra = aPhoneNo.length() -10;
                aPhoneNo = aPhoneNo.substring(extra);
            }
        }

        return aPhoneNo;
    }
}
