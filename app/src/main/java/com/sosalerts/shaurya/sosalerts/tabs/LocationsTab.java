package com.sosalerts.shaurya.sosalerts.tabs;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.R;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.address.SavedLocations;
import com.sosalerts.shaurya.sosalerts.services.util.GetLocationCordinatesService;
import com.sosalerts.shaurya.sosalerts.services.util.ReadOut;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/**
 * Created by shaurya on 1/23/2017.
 */

public class LocationsTab extends Fragment {
    View view = null;
    ListView listView ;
    public static String actionName = "LocationsTab";
    private final String fileName = this.getClass().getSimpleName();
    public static final String PACKAGE_NAME =         "com.sosalerts.shaurya.sosalerts.tabs.LocationsTab";
    public static String savedLocationName = PACKAGE_NAME+".savedLocationName";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.locations_tab,   container, false);
        Button button = (Button)  view.findViewById(R.id.btn_savelocation);
        EditText text = (EditText) view.findViewById(R.id.location_name);
        text.setTextColor(Color.WHITE);
        text.setHintTextColor(Color.CYAN);
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
        Log.e(fileName, "getSavedLocations  "+savedLocation);
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
                    android.R.layout.simple_list_item_1, android.R.id.text1,savedLocationsArray){
                @Override
                public View getView(int position, View convertView, ViewGroup parent){
                    // Get the Item from ListView
                    View view = super.getView(position, convertView, parent);

                    // Initialize a TextView for ListView each Item
                    TextView tv = (TextView) view.findViewById(android.R.id.text1);

                    // Set the text color of TextView (ListView Item)
                    tv.setTextColor(Color.WHITE);
                    tv.setBackground( ResourcesCompat.getDrawable(getResources(),R.drawable.black,null));
                    // Generate ListView Item using TextView
                    return view;
                }
            };

            listView = (ListView) view.findViewById(R.id.savedlocationview);

            listView.setAdapter(adapter);
            LocationDeleteListner listner = new LocationDeleteListner();
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
        Intent saveLocationIntent = new Intent(v.getContext(), GetLocationCordinatesService.class);
        saveLocationIntent.putExtra(savedLocationName,locationName);
        v.getContext().startService(saveLocationIntent);
        Intent speakIntent = new Intent(getActivity(), ReadOut.class);
        speakIntent.putExtra(ReadOut.textToSpeak,"If you click on a location you will be able to view that on map. In order to delete a saved location you need to press it for a longer duration of time.");
        speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
        getActivity().startService(speakIntent);
     }
}

