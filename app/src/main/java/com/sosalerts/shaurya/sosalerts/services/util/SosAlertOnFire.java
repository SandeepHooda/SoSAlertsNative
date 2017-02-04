package com.sosalerts.shaurya.sosalerts.services.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.sosalerts.shaurya.sosalerts.MainActivity;

/**
 * Created by shaurya on 2/4/2017.
 */

public class SosAlertOnFire extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE: // Yes button clicked
                        MainActivity.sosAlertOnFire = false;
                        break;

                } } };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Do you feel safe now?");
        builder.setMessage("Is danger over now") .setPositiveButton("Yes", dialogClickListener) .show();

    }
}
