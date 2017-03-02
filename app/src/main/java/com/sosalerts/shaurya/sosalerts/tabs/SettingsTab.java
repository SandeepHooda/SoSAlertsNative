package com.sosalerts.shaurya.sosalerts.tabs;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
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
    public static boolean replyToWhereAreYouSettings = false;
    public static boolean useAndroidLocationSettings = false;
    public static int powerButtonSettings = Storage.settingsPowerButtonCountDefault;
    public static double safeZoneBoundrySettings = Storage.settingsSafeZoneBoundryDefault;
    public static String locationTrackerFrequencySettings = Storage.settingsLocationTrackerFrequencyDefault;



    @Override
    public void onResume() {
        super.onResume();
        /*Button showMeOnMap = (Button) view.findViewById(R.id.showMeOnMap);
        showMeOnMap.setText(getLocationButtontext());
        Log.e(fileName,"Button text update");*/

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.settings_tab, container, false);

        //Respond to replyToFindMyPhone
        ToggleButton replyToFindMyPhone = (ToggleButton) view.findViewById(R.id.replyToFindMyPhone);
        replyToFindMyPhone.setHintTextColor(Color.WHITE);
        replyToFindMyPhone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e(fileName, "replyToFindMyPhone  "+isChecked );
                Storage.storeinDB(Storage.replyToFindMyPhone,""+isChecked,getActivity());

            }
        });
        if(Storage.getFromDB(Storage.replyToFindMyPhone,getActivity()) == null){
            Storage.storeinDB(Storage.replyToFindMyPhone,"true",getActivity());
        }

        replyToFindMyPhone.setChecked(Boolean.parseBoolean(Storage.getFromDB(Storage.replyToFindMyPhone,getActivity())));


        //Auto updates for replyToWhereAreYouSettings
        ToggleButton replyToWhereAreYou = (ToggleButton) view.findViewById(R.id.replyToWhereAreYou);
        replyToWhereAreYou.setHintTextColor(Color.WHITE);
        replyToWhereAreYou.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e(fileName, "replyToWhereAreYou  "+isChecked );
                replyToWhereAreYouSettings = isChecked;
                Storage.storeinDB(Storage.settingsreplyToWhereAreYou,""+isChecked,getActivity());

            }
        });
        if(Storage.getFromDB(Storage.settingsreplyToWhereAreYou,getActivity()) == null){
            Storage.storeinDB(Storage.settingsreplyToWhereAreYou,"true",getActivity());
        }
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
        Storage.settingsSafeZoneBoundryDefault = Double.parseDouble( getActivity().getResources().getStringArray(R.array.safe_zone_array)[0]);
        safeZoneBoundrySettings = Storage.settingsSafeZoneBoundryDefault;
        Log.e(fileName, "default geo fencing "+safeZoneBoundrySettings );
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
            safeZoneBoundrySettings = Double.parseDouble(Storage.getFromDB(Storage.settingsSafeZoneBoundry,getActivity()));
        }catch (Exception e){
            safeZoneBoundrySettings = Storage.settingsSafeZoneBoundryDefault;
        }

        if(safeZoneBoundrySettings == 1500){
            safeZoneBoundry.setSelection(6);
        }else if(safeZoneBoundrySettings == 2000){
            safeZoneBoundry.setSelection(7);
        }else if(safeZoneBoundrySettings == 200){
            safeZoneBoundry.setSelection(0);
        }else {
            for(int i=5; i<=10; i++){
                if(i*100 == safeZoneBoundrySettings){
                    safeZoneBoundry.setSelection(i-5+1);
                    break;
                }
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

       /* //use android location API
        ToggleButton useAndroidLocation = (ToggleButton) view.findViewById(R.id.useAndroidLocation);
        useAndroidLocation.setHintTextColor(Color.WHITE);
        useAndroidLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e(fileName, "useAndroidLocation  "+isChecked);
                useAndroidLocationSettings = isChecked;
                Storage.storeinDB(Storage.useAndroidLocation,""+isChecked,getActivity());

            }
        });
        useAndroidLocationSettings = Boolean.parseBoolean(Storage.getFromDB(Storage.useAndroidLocation,getActivity()));
        useAndroidLocation.setChecked(useAndroidLocationSettings);*/

        //Speak location
        ToggleButton speakLocation = (ToggleButton) view.findViewById(R.id.speakLocation);
        speakLocation.setHintTextColor(Color.WHITE);
        speakLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e(fileName, "useAndroidLocation  "+isChecked);
                useAndroidLocationSettings = isChecked;
                Storage.storeinDB(Storage.speakLocation,""+isChecked,getActivity());

            }
        });
        speakLocation.setChecked(Boolean.parseBoolean(Storage.getFromDB(Storage.speakLocation,getActivity())));

        //Smart battery saver
        final ToggleButton smartBatterySaver = (ToggleButton) view.findViewById(R.id.smartBatteryMode);
        smartBatterySaver.setHintTextColor(Color.WHITE);
        smartBatterySaver.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e(fileName, "smartBatterySaver  "+isChecked);
                Storage.storeinDB(Storage.smartbatteryMode,""+isChecked,getActivity());

            }
        });
        smartBatterySaver.setChecked(Boolean.parseBoolean(Storage.getFromDB(Storage.smartbatteryMode,getActivity())));


        return view;
    }



}
