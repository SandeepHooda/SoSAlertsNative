package com.sosalerts.shaurya.sosalerts.db;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.R;
import com.sosalerts.shaurya.sosalerts.tabs.LocationsTab;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by shaurya on 1/24/2017.
 */

public class Storage {
    public static final String savedLocations = "SavedLocations";
    public static final String currentAction = "currentAction";

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

        Set<String> savedLocation =  sharedPref.getStringSet(itemName, null);
        if (null == savedLocation){
            savedLocation = new HashSet<String>();
        }
        savedLocation.add(itemValue);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.putStringSet(itemName,savedLocation);
        editor.commit();

        refreshLocationsTab(activity);

    }
    private static void refreshLocationsTab(Context activity){
        Intent locationsIntent = new Intent(activity, MainActivity.class);
        locationsIntent.putExtra(MainActivity.orignationActivityName, LocationsTab.actionName);
        activity.startActivity(locationsIntent);// to get user cordinates

    }
    public static void deleteinDBStringSet(String itemName, String itemValue, Context activity){

        SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.saved_location_db),activity.MODE_PRIVATE);

        Set<String> savedLocation =  sharedPref.getStringSet(itemName, null);
        if (null != savedLocation){
            savedLocation.remove(itemValue);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.putStringSet(itemName,savedLocation);
            editor.commit();
            Toast.makeText(activity," Location Deleted!" , Toast.LENGTH_LONG)
                    .show();
            refreshLocationsTab(activity);
        }

    }
    public static Set<String> getFromDBDBStringSet(String itemName,  Context activity){
        SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.saved_location_db),activity.MODE_PRIVATE);
        return  sharedPref.getStringSet(itemName, null);

    }
}
