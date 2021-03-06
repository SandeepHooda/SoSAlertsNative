package com.sosalerts.shaurya.sosalerts.services.powerbutton;

/**
 * Created by shaurya on 1/21/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.address.AddressResultReceiver;
import com.sosalerts.shaurya.sosalerts.services.util.GetLocationCordinatesService;
import com.sosalerts.shaurya.sosalerts.services.util.ReadOut;
import com.sosalerts.shaurya.sosalerts.services.util.SosAlertOnFireDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class ScreenReceiver extends BroadcastReceiver {


    private Date powerButtonLastPressed = new Date();
    private short powerButtonPressCount = 0;
    private int triggerAlertAfterCount =5; //trigget SOS alert after three times power button
    public static final String SOSAlert = "SOSAlert";
    private final String fileName = this.getClass().getSimpleName();

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction()) || Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            ReadOut.phoneFound = true;

            try{
                triggerAlertAfterCount = Integer.parseInt(Storage.getFromDB(Storage.settingsPowerButtonCount,context));
            }catch (Exception e){
                triggerAlertAfterCount = 5;
            }

            powerButtonPressCount++;
            Log.e(fileName, "power button pressed " + powerButtonPressCount);
            //if pressed three times  in one minute
            if (new Date().getTime() - powerButtonLastPressed.getTime() < 60000) {
                if (powerButtonPressCount >= triggerAlertAfterCount) {
                    powerButtonPressCount = 0;

                    List<String> contactList = Storage.getEmergencyContactsList(context);
                    if (null != contactList && contactList.size() >0){
                        String firstPhone = contactList.get(0);

                        if(!MainActivity.testMode){
                            //call a phone
                            Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                            phoneIntent.setData(Uri.parse("tel:"+Storage.getOnlyNumbers(firstPhone)));
                            phoneIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            context.startActivity(phoneIntent);
                        }


                    }

                    Intent dialogIntent = new Intent(context, SosAlertOnFireDialog.class);
                    dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(dialogIntent);
                    String myemergencyContacts = Storage.getEmergencyContacts(context);
                    MainActivity.sosAlertOnFire = true;
                    //SMS to all
                    if(Storage.isLocationTrackerOn(context)){//Use last known location
                        //List<String> myemergencyContactsList, String address, String cordinates
                        StringTokenizer tokenizer = new StringTokenizer(myemergencyContacts, ",") ;
                        List<String> myemergencyContactsList = new ArrayList<String>();
                        while(tokenizer.hasMoreTokens()){
                            String number = tokenizer.nextToken();
                            if(null != number && number.trim().length() > 0){
                                myemergencyContactsList.add(number);
                            }

                        }
                        String lastKnownLocation = Storage.getFromDB(Storage.lastKnownLocation,context);
                        lastKnownLocation += " at "+Storage.getFromDB(Storage.lastKnownLocationTime,context);
                        String address = Storage.getFromDB(Storage.lastKnownLocationAddress,context);
                        AddressResultReceiver.smsToAll(myemergencyContactsList, address, lastKnownLocation);
                    }else {

                        AddressResultReceiver.smsToAll(Storage.getEmergencyContactsList(context), null, null);
                        Intent locationCordinatesIntent = new Intent(context, GetLocationCordinatesService.class);
                        String  ChainOfDuty = GetLocationCordinatesService.ChainOfDuty_Address+","+GetLocationCordinatesService.ChainOfDuty_SMS_AllContact;
                        locationCordinatesIntent.putExtra(GetLocationCordinatesService.ChainOfDuty, ChainOfDuty);
                        locationCordinatesIntent.putExtra(MainActivity.orignationActivityName, SOSAlert);
                        locationCordinatesIntent.putExtra(GetLocationCordinatesService.myemergencyContactsNumbers,myemergencyContacts);//myemergencyContacts
                        context.startService(locationCordinatesIntent);
                    }


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
            ReadOut.phoneFound = true;

        }
    }




}