package com.sosalerts.shaurya.sosalerts.tabs;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.R;
import com.sosalerts.shaurya.sosalerts.db.Storage;

import java.util.Set;

/**
 * Created by shaurya on 1/23/2017.
 */

public class LocationsTab extends Fragment {
    View view = null;
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

        addSavedLocation();
        return view;
    }

    private void addSavedLocation(){
        Set<String> savedLocation = Storage.getFromDBDBStringSet(Storage.savedLocations,view.getContext());
        Button myButton = new Button(view.getContext());
        myButton.setText("Push Me");

        LinearLayout ll = (LinearLayout) view.findViewById(R.id.savelocationtab);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        ll.addView(myButton, lp);
    }

    public void saveLocation(View v) {
        EditText editText = (EditText) view.findViewById(R.id.location_name);
        String message = editText.getText().toString();

        /*SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.saved_location_db),view.getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Set<String> savedLocation =  sharedPref.getStringSet("SavedLocations", null);
        if (null == savedLocation){
            savedLocation = new HashSet<String>();
            Log.e("LOB", "it was null ");
        }
        savedLocation.add(message);
        editor.putStringSet("SavedLocations",savedLocation);
        editor.commit();
        Log.e("LOB", "save ? "+sharedPref.getStringSet("SavedLocations", null));*/

        Intent mainActivityIntent = new Intent(view.getContext(), MainActivity.class);
        mainActivityIntent.putExtra("IntentAction", "SaveLocation");
        mainActivityIntent.putExtra(Storage.locationName, message);

       startActivity(mainActivityIntent);// to get user cordinates

    }
}

