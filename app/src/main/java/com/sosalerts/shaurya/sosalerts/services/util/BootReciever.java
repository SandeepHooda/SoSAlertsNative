package com.sosalerts.shaurya.sosalerts.services.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sosalerts.shaurya.sosalerts.MainActivity;

/**
 * Created by shaurya on 2/3/2017.
 */

public class BootReciever  extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myIntent = new Intent(context, MainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);
    }

}
