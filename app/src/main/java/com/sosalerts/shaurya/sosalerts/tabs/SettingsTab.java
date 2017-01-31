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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.settings_tab, container, false);

        //Auto updates for saved locations
        Button showMeOnMap = (Button) view.findViewById(R.id.showMeOnMap);
        String location = Storage.getFromDB(Storage.lastKnownLocationDistance,getActivity())+"\n"+ Storage.getFromDB(Storage.lastKnownLocationTime,getActivity());

        if(null != location && location.indexOf("@") >0){
            location = location.substring(location.indexOf("@")+1);
        }
        showMeOnMap.setText(location);
        showMeOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String   mapLink= "https://www.google.com/maps/place/@"+Storage.getFromDB(Storage.lastKnownLocation,getActivity());;
                Intent mapIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(mapLink));
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);


            }
        });


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

        //use android location API
        ToggleButton useAndroidLocation = (ToggleButton) view.findViewById(R.id.useAndroidLocation);
        useAndroidLocation.setHintTextColor(Color.WHITE);
        useAndroidLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e(fileName, "useAndroidLocation  "+isChecked +" "+buttonView.getText());
                useAndroidLocationSettings = isChecked;
                Storage.storeinDB(Storage.useAndroidLocation,""+isChecked,getActivity());

            }
        });
        useAndroidLocationSettings = Boolean.parseBoolean(Storage.getFromDB(Storage.useAndroidLocation,getActivity()));
        useAndroidLocation.setChecked(useAndroidLocationSettings);


        return view;
    }


}
