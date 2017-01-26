package com.sosalerts.shaurya.sosalerts;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Handler;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.address.AddressResultReceiver;
import com.sosalerts.shaurya.sosalerts.services.address.FetchAddressIntentService;
import com.sosalerts.shaurya.sosalerts.services.powerbutton.LockService;
import com.sosalerts.shaurya.sosalerts.services.powerbutton.ScreenReceiver;
import com.sosalerts.shaurya.sosalerts.services.sms.IncomingSms;
import com.sosalerts.shaurya.sosalerts.services.sms.ReadOut;
import com.sosalerts.shaurya.sosalerts.tabs.ContactsTab;
import com.sosalerts.shaurya.sosalerts.tabs.LocationsTab;
import com.sosalerts.shaurya.sosalerts.tabs.PagerAdapter;

import android.Manifest;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener , AddressResultReceiver.Receiver{

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
    private String smsSenderPhoneNo= null;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    public static TextToSpeech myTTS;

    public static boolean phoneFound = false;
    public static Map<String,String> allContacts = new HashMap<String,String>();
    public static Set<String> myemergencyContacts = new HashSet<>();
    public static final boolean testMode = true;
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
         if (myTTS != null) {
            myTTS.stop();
            myTTS.shutdown();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.e(fileName, "Text to speach  Status $$$$$$$$$$$$$$$$$ "+status);
            }
        });
        //addressResultReceiver = new AddressResultReceiver(new Handler());
        Intent intent = getIntent();
        intentOriginator = intent.getStringExtra(orignationActivityName);
        createTabs(intentOriginator);


        Log.e(fileName, "Main activity Intent action  "+intentOriginator);
        if(!oneTimeActivityStarted){
            oneTimeActivityStarted = true;

            checkPermissions();
            readContacts();

           // Log.e(fileName, "Registered Address listner ");
           startService(new Intent(getApplicationContext(), LockService.class));//Power button service
        }
        if(ScreenReceiver.SOSAlert.equals(intentOriginator) || IncomingSms.whereAreYou.equals(intentOriginator) ){
            smsSenderPhoneNo = intent.getStringExtra(IncomingSms.phoneNo);
           userLocationFacade(null);
        }
        if(IncomingSms.findMyPhone.equals(intentOriginator)){
            Intent speakIntent = new Intent(this, ReadOut.class);
            startService(speakIntent);
        }
        /*if(IncomingSms.whereAreYou.equals(intentAction)){
            Intent speakIntent = new Intent(this, ReadOut.class);
            speakIntent.putExtra(MainActivity.orignationActivityName, IncomingSms.whereAreYou);
            speakIntent.putExtra(IncomingSms.phoneNo, intent.getStringExtra(IncomingSms.phoneNo));
            startService(speakIntent);
        }*/

    }


   public void userLocationFacade(String nameFromSaveLocationTab){
       if(!IncomingSms.whereAreYou.equals(intentOriginator)){
           smsSenderPhoneNo = null;
       }
       this.currentLocationName = nameFromSaveLocationTab;
       // First we need to check availability of play services
       if (checkPlayServices()) {
           Log.e(fileName, "buildGoogleApiClient");
           // Building the GoogleApi client
           buildGoogleApiClient();
           getUserLocation();
       }
   }
    private void startAddressIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.LOCATION_DATA_CORDINATES, mLastLocation);
        intent.putExtra(FetchAddressIntentService.ADDRESS_RESULT_RECEIVER, addressResultReceiver);
        intent.putExtra(orignationActivityName, intentOriginator);
        if(IncomingSms.whereAreYou.equals(intentOriginator)){
            intent.putExtra(IncomingSms.phoneNo, smsSenderPhoneNo);
        }
        startService(intent);
    }

    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)     != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,  Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.SEND_SMS},  MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)      != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)      != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,    new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(MainActivity.this,  Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_LOCATION);
        }

    }


    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }
    private void getUserLocation(){
        Log.e(fileName, " getting location ");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
    private void displayLocation() {
        Log.e(fileName, " display location ");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        Log.e(fileName, " mLastLocation "+mLastLocation);
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            Log.e(fileName, "location ::::: "+latitude+ " --- "+longitude);
            String location = latitude+","+longitude;
            if (this.currentLocationName != null){
                Storage.storeinDBStringSet(Storage.savedLocations, this.currentLocationName +"_"+"https://www.google.com/maps/place/@"+location+",16z",this);
            }else {
                startAddressIntentService();
            }

        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(resultCode)) {
                googleAPI.getErrorDialog(this, resultCode,        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else {
                Toast.makeText(getApplicationContext(),     "This device is not supported.", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }


    private void createTabs(String tabName){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Contacts"));
        tabLayout.addTab(tabLayout.newTab().setText("Locations"));
        tabLayout.addTab(tabLayout.newTab().setText("Trip"));
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
                phoneNumber = phoneNumber.replaceAll("[^\\d]", "").trim();
                if (phoneNumber.length() > 10){
                    int extra = phoneNumber.length() -10;
                    phoneNumber = phoneNumber.substring(extra);
                }
                //Log.e(fileName, "name :"+name+" phoneNumber "+phoneNumber);
                allContacts.put(phoneNumber,name);
            }

        }

    }
}
