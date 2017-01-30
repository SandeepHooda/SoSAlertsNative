package com.sosalerts.shaurya.sosalerts.tabs;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.sosalerts.shaurya.sosalerts.R;
import com.sosalerts.shaurya.sosalerts.db.Storage;

/**
 * Created by shaurya on 1/23/2017.
 */

public class SettingsTab extends Fragment {
    private final String fileName = this.getClass().getSimpleName();
    View view = null;
    ListView listView ;
    public static String actionName = "SettingsTab";
    public static boolean locationAutoUpdatesSettings = false;
    public static boolean replyToWhereAreYouSettings = false;
    public static int powerButtonSettings = Storage.settingsPowerButtonCountDefault;
    public static int safeZoneBoundrySettings = Storage.settingsSafeZoneBoundryDefault;
    public static String locationTrackerFrequencySettings = Storage.settingsLocationTrackerFrequencyDefault;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.settings_tab, container, false);

        //Auto updates for saved locations
        ToggleButton locationAutoUpdate = (ToggleButton) view.findViewById(R.id.locationAutoUpdate);
        locationAutoUpdate.setHintTextColor(Color.WHITE);
        locationAutoUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e(fileName, "getSavedLocations  "+isChecked +" "+buttonView.getText());
                locationAutoUpdatesSettings = isChecked;
                Storage.storeinDB(Storage.settingsLocationAutoUpdates,""+isChecked,getActivity());

            }
        });
        locationAutoUpdatesSettings = Boolean.parseBoolean(Storage.getFromDB(Storage.settingsLocationAutoUpdates,getActivity()));
        locationAutoUpdate.setChecked(locationAutoUpdatesSettings);

        //Auto updates for replyToWhereAreYouSettings
        ToggleButton replyToWhereAreYou = (ToggleButton) view.findViewById(R.id.replyToWhereAreYou);
        replyToWhereAreYou.setHintTextColor(Color.WHITE);
        replyToWhereAreYou.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e(fileName, "replyToWhereAreYou  "+isChecked +" "+buttonView.getText());
                replyToWhereAreYouSettings = isChecked;
                Storage.storeinDB(Storage.settingsreplyToWhereAreYou,""+isChecked,getActivity());

            }
        });
        replyToWhereAreYouSettings = Boolean.parseBoolean(Storage.getFromDB(Storage.settingsreplyToWhereAreYou,getActivity()));
        replyToWhereAreYou.setChecked(replyToWhereAreYouSettings);

        //powerButton simultanious press
        Spinner powerbuttonCount = (Spinner) view.findViewById(R.id.powerbuttonCount);
        powerbuttonCount.setBackgroundColor(Color.WHITE);
        powerbuttonCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                powerButtonSettings = Integer.parseInt(parent.getItemAtPosition(pos).toString());
                Storage.storeinDB(Storage.settingsPowerButtonCount,""+powerButtonSettings,getActivity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){}
        });

        try{
            powerButtonSettings = Integer.parseInt(Storage.getFromDB(Storage.settingsPowerButtonCount,getActivity()));
        }catch (Exception e){
            powerButtonSettings = 5;
        }

        for(int i=5; i<=10; i++){
            if(i == powerButtonSettings){
                powerbuttonCount.setSelection(i-5);
                break;
            }
        }


        //Safe zone boundry
        Spinner safeZoneBoundry = (Spinner) view.findViewById(R.id.safeZoneBoundry);
        safeZoneBoundry.setBackgroundColor(Color.WHITE);
        safeZoneBoundry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                safeZoneBoundrySettings = Integer.parseInt(parent.getItemAtPosition(pos).toString());
                Storage.storeinDB(Storage.settingsSafeZoneBoundry,""+safeZoneBoundrySettings,getActivity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){}
        });

        try{
            safeZoneBoundrySettings = Integer.parseInt(Storage.getFromDB(Storage.settingsSafeZoneBoundry,getActivity()));
        }catch (Exception e){
            safeZoneBoundrySettings = Storage.settingsSafeZoneBoundryDefault;
        }

        for(int i=2; i<=11; i++){
            if(safeZoneBoundrySettings < 2000){
                if(i*100 == safeZoneBoundrySettings){
                    safeZoneBoundry.setSelection((i-2));
                    break;
                }

            }else {
                safeZoneBoundry.setSelection(9);
                break;
            }
        }


        //Location tracker frequency
        Spinner locationTrackerFrequency = (Spinner) view.findViewById(R.id.locationTrackerFrequency);
        locationTrackerFrequency.setBackgroundColor(Color.WHITE);
        locationTrackerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                locationTrackerFrequencySettings = parent.getItemAtPosition(pos).toString();
                Storage.storeinDB(Storage.settingsLocationTrackerFrequency,locationTrackerFrequencySettings,getActivity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){}
        });
        locationTrackerFrequencySettings = Storage.getFromDB(Storage.settingsLocationTrackerFrequency,getActivity());
        if (null == locationTrackerFrequencySettings){
            locationTrackerFrequencySettings = Storage.settingsLocationTrackerFrequencyDefault;
        }

        String[] freq = getActivity().getResources().getStringArray(R.array.locationTrackerFrequency_array);
        for(int i=0; i<3; i++){
            if(locationTrackerFrequencySettings.equals(freq[i])){
                locationTrackerFrequency.setSelection(i);
                 break;
            }

        }


        return view;
    }


}
