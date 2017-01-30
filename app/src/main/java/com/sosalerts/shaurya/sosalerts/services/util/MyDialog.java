package com.sosalerts.shaurya.sosalerts.services.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by shaurya on 1/26/2017.
 */

public class MyDialog extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE: // Yes button clicked
                        ReadOut.phoneFound = true;
                        break;
                    case DialogInterface.BUTTON_NEGATIVE: // No button clicked // do nothing
                        ReadOut.phoneFound = true;
                        break;
                } } };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Looking for me?");
        builder.setMessage("Glad that you me") .setPositiveButton("Me too!", dialogClickListener) .setNegativeButton("Thanks", dialogClickListener).show();

    }
}
