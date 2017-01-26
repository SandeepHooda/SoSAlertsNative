package com.sosalerts.shaurya.sosalerts.tabs;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.R;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.address.SavedLocations;
import com.sosalerts.shaurya.sosalerts.services.sms.ReadOut;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

/**
 * Created by shaurya on 1/23/2017.
 */

public class LocationsTab extends Fragment {
    View view = null;
    ListView listView ;
    public static String actionName = "LocationsTab";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.locations_tab,   container, false);
        Button button = (Button)  view.findViewById(R.id.btn_savelocation);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLocation(view);
            }
        });

        getSavedLocations();
        return view;
    }

    private void getSavedLocations(){

       Set<String> savedLocation = Storage.getFromDBDBStringSet(Storage.savedLocations,getActivity());
        Log.e("LOB tab", "getSavedLocations  "+savedLocation);
        if (savedLocation != null && savedLocation.size() > 0){
            List<SavedLocations> savedLocationList = new ArrayList<SavedLocations>();
            for (String alocation: savedLocation){
                int seperatorLocation = alocation.indexOf("_");
                String locationName = alocation.substring(0,seperatorLocation);
                String link = alocation.substring(seperatorLocation+1);
                SavedLocations location = new SavedLocations();
                location.setLocationName(locationName);
                location.setLink(link);
                savedLocationList.add(location);

            }
            SavedLocations[] savedLocationsArray  = new SavedLocations[savedLocationList.size()];
            savedLocationsArray = savedLocationList.toArray(savedLocationsArray);
            ArrayAdapter<SavedLocations> adapter;
            adapter = new ArrayAdapter<SavedLocations>(view.getContext(),
                    android.R.layout.simple_list_item_1, android.R.id.text1,savedLocationsArray);

            listView = (ListView) view.findViewById(R.id.savedlocationview);

            listView.setAdapter(adapter);
            ListOnItemClickListener listner = new ListOnItemClickListener();
            listner.listView = listView;
            //listView.setOnTouchListener( listner);
            listView.setOnItemLongClickListener( listner);

            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    // ListView Clicked item index
                    int itemPosition     = position;

                    // ListView Clicked item value
                    SavedLocations  itemValue    = (SavedLocations) listView.getItemAtPosition(position);
                    if(itemValue.getLongPressTime() == null || (new Date().getTime() -itemValue.getLongPressTime().getTime() > 4000)){

                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(itemValue.getLink()));
                        startActivity(browserIntent);
                    }


                }

            });
        }


    }

    public void saveLocation(View v) {
        EditText editText = (EditText) view.findViewById(R.id.location_name);
        String locationName = editText.getText().toString();

        ((MainActivity)getActivity()).userLocationFacade(locationName);



    }
}

