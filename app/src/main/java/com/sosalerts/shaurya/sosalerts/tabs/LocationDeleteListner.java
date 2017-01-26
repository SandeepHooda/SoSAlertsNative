package com.sosalerts.shaurya.sosalerts.tabs;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;

import android.widget.ListView;
import android.widget.Toast;

import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.address.SavedLocations;

import java.util.Date;

/**
 * Created by shaurya on 1/25/2017.
 */

public class LocationDeleteListner implements   AdapterView.OnItemLongClickListener {
    public ListView listView ;


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
        final SavedLocations  itemValue    = (SavedLocations) listView.getItemAtPosition(position);
        itemValue.setLongPressTime(new Date());
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: // Yes button clicked
                    Storage.deleteinDBStringSet(Storage.savedLocations,itemValue.getLocationName()+"_"+itemValue.getLink(),view.getContext());
                    break;
                case DialogInterface.BUTTON_NEGATIVE: // No button clicked // do nothing
                     Toast.makeText(view.getContext(), "No Clicked", Toast.LENGTH_LONG).show();
                    break;
            } } };
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage("Do you want to delete "+itemValue.getLocationName()+" ?") .setPositiveButton("Yes", dialogClickListener) .setNegativeButton("No", dialogClickListener).show();




        return false;
    }
}
