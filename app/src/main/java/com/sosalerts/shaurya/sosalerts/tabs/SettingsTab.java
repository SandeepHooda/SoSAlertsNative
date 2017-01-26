package com.sosalerts.shaurya.sosalerts.tabs;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.settings_tab, container, false);
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

        Log.e(fileName, "locationAutoUpdates  "+locationAutoUpdatesSettings);
        return view;
    }


}
