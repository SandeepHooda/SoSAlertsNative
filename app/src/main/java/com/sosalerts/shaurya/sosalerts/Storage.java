package com.sosalerts.shaurya.sosalerts;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by shaurya on 1/24/2017.
 */

public class Storage {
    public static final String savedLocations = "SavedLocations";
    public static final String currentAction = "currentAction";
    public static final String locationName = "locationName";
    public static void storeinDB(String itemName, String itemValue, FragmentActivity activity){
        SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.saved_location_db),activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(itemName,itemValue);
        editor.commit();
    }
    public static String getFromDB(String itemName,  Context activity){
        SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.saved_location_db),activity.MODE_PRIVATE);
        return sharedPref.getString(itemName, null);
    }
    public static void storeinDBStringSet(String itemName, String itemValue, Context activity){
        SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.saved_location_db),activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Set<String> savedLocation =  sharedPref.getStringSet(itemName, null);
        if (null == savedLocation){
            savedLocation = new HashSet<String>();
            Log.e("LOB", "it was null ");
        }
        Log.e("LOB", "saving  "+itemName+" itemValue = "+itemValue);
        savedLocation.add(itemValue);
        editor.putStringSet(itemName,savedLocation);
        editor.commit();
    }
    public static Set<String> getFromDBDBStringSet(String itemName,  Context activity){
        SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.saved_location_db),activity.MODE_PRIVATE);
        return  sharedPref.getStringSet(itemName, null);

    }
}
