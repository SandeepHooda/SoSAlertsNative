package com.sosalerts.shaurya.sosalerts;



import android.content.Context;

import android.content.Intent;


import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.storage.StorageTask;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.address.AddressResultReceiver;
import com.sosalerts.shaurya.sosalerts.services.address.FetchAddressIntentService;
import com.sosalerts.shaurya.sosalerts.services.powerbutton.LockService;
import com.sosalerts.shaurya.sosalerts.services.util.ReadOut;
import com.sosalerts.shaurya.sosalerts.tabs.ContactsTab;
import com.sosalerts.shaurya.sosalerts.tabs.LocationsTab;
import com.sosalerts.shaurya.sosalerts.tabs.PagerAdapter;
import android.Manifest;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements AddressResultReceiver.Receiver{



    public AddressResultReceiver addressResultReceiver;
    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    private static final int REQUEST_LOCATION = 2;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static boolean oneTimeActivityStarted = false;
    private String currentLocationName = "";
    private String currentLocation = null;
    private Location mLastLocation;
    public static final String orignationActivityName = "orignationActivityName";
    private String intentOriginator;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    public static Map<String,String> allContacts = new HashMap<String,String>();
    public static final boolean testMode = false;
    private final String fileName = "MainActivity : ";
    @Override
    protected  void onResume(){
        super.onResume();
        addressResultReceiver = new AddressResultReceiver(new Handler());
        addressResultReceiver.setReceiver(this);
    }
    @Override
    protected  void onDestroy(){
        super.onPause();
        addressResultReceiver.setReceiver(null);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent intent = getIntent();
        intentOriginator = intent.getStringExtra(orignationActivityName);
        createTabs(intentOriginator);


        Log.e(fileName, "Main activity Intent action  "+intentOriginator);
        if(!oneTimeActivityStarted){
            oneTimeActivityStarted = true;
            checkPermissions();
            readContacts();
            startService(new Intent(getApplicationContext(), LockService.class));//Power button service
            checkSavedcontacts();
        }

    }


    private void checkSavedcontacts(){
        List<String> contacts = Storage.getEmergencyContactsList(this);
        if(null == contacts || contacts.size() ==0){
            Intent speakIntent = new Intent(this, ReadOut.class);
            speakIntent.putExtra(ReadOut.textToSpeak,"Please enter atleast one emergency contact phone number from contacts tab.");
            speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
            startService(speakIntent);
        }

    }

    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)     != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,  Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.SEND_SMS},  MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK)      != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,    new String[]{Manifest.permission.WAKE_LOCK}, REQUEST_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)      != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,    new String[]{Manifest.permission.RECEIVE_SMS}, REQUEST_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)      != PackageManager.PERMISSION_GRANTED) {
            Log.e(fileName ,"donot have SMS read permission");
            ActivityCompat.requestPermissions(this,    new String[]{Manifest.permission.READ_SMS}, REQUEST_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE)      != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,    new String[]{Manifest.permission.VIBRATE}, REQUEST_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)      != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)      != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,    new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(MainActivity.this,  Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this,  Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,    new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED}, REQUEST_LOCATION);
        }else{
            Log.e(fileName ,"Has boot permission");
        }


        /*Intent in = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(in);*/
    }




    private void createTabs(String tabName){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Contacts"));
        tabLayout.addTab(tabLayout.newTab().setText("Locations"));
        tabLayout.addTab(tabLayout.newTab().setText("Settings"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        Log.e(fileName, "Tab to open "+tabName);
        if (LocationsTab.actionName.equals(tabName)){
            viewPager.setCurrentItem(1);
        }
        if (ContactsTab.actionName.equals(tabName)){
            viewPager.setCurrentItem(0);
        }

    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {


        Log.e(fileName, "Main activity got the result "+resultData.getString(FetchAddressIntentService.Location_ADDRESS));

    }
    private void readContacts(){
        Cursor cursor = getContentResolver().query(   ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,null, null);

        //now we have cusror with contacts and get diffrent value from cusror.

        while (cursor.moveToNext()) {
            String name =cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if(null != phoneNumber){
                phoneNumber = Storage.getOnlyNumbersLastTen(phoneNumber.trim());
                //Log.e(fileName, "name :"+name+" phoneNumber "+phoneNumber);
                allContacts.put(phoneNumber,name);
            }

        }

    }
}
