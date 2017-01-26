package com.sosalerts.shaurya.sosalerts.services.powerbutton;

/**
 * Created by shaurya on 1/21/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.sosalerts.shaurya.sosalerts.MainActivity;

import java.util.Date;

public class ScreenReceiver extends BroadcastReceiver {


    private Date powerButtonLastPressed = new Date();
    private short powerButtonPressCount = 0;
    private short triggerAlertAfterCount =5; //trigget SOS alert after three times power button
    public static final String SOSAlert = "SOSAlert";
    private final String fileName = this.getClass().getName();

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction()) || Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {


            powerButtonPressCount++;
            Log.e(fileName, "power button pressed " + powerButtonPressCount);
            //if pressed three times  in one minute
            if (new Date().getTime() - powerButtonLastPressed.getTime() < 60000) {
                if (powerButtonPressCount >= triggerAlertAfterCount) {
                    powerButtonPressCount = 0;

                    //Danger
                    Intent mainActivityIntent = new Intent(context, MainActivity.class);
                    mainActivityIntent.putExtra(MainActivity.orignationActivityName, SOSAlert);
                    mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Log.e(fileName, "Staring main activity with danger flag");
                    //context.startService(new Intent(context, FetchAddressIntentService.class));//Fetch address service
                    SmsManager smsManager = SmsManager.getDefault();
                    // smsManager.sendTextMessage("540", null, "I am in danger. ", null, null);
                    Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                    phoneIntent.setData(Uri.parse("tel:9216411835"));
                    phoneIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    //context.startActivity(phoneIntent);
                    context.startActivity(mainActivityIntent);// to get user cordinates
                    //getUserLocationCordinates()
                }
            } else {
                Log.e(fileName, "First time power pressed "+powerButtonPressCount);
                powerButtonPressCount = 1;
                powerButtonLastPressed = new Date();
            }


            /*Intent smsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + "9216411835"));
            smsIntent.putExtra("sms_body", "Hello Native developer");
            smsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(smsIntent);*/


        } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Log.e(fileName, "Sandeep no sms Screen receiver userpresent");
           /* Log.e(fileName," Sandeep Screen receiver wasScreenOn "+wasScreenOn);
            Intent main = new Intent(context, MainActivity.class);
            main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            main.putExtra("sandeep.sms", "Hello android native developer");
            context.startActivity(main);*/

        }
    }




}