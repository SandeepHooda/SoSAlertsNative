package com.sosalerts.shaurya.sosalerts;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
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
import com.sosalerts.shaurya.sosalerts.tabs.PagerAdapter;

import android.Manifest;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;





public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener , AddressResultReceiver.Receiver{
    private String currentAction = null;
    public AddressResultReceiver addressResultReceiver;
    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    private static final int REQUEST_LOCATION = 2;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static boolean oneTimeActivityStarted = false;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private TextToSpeech ttobj;

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




        ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });

        //addressResultReceiver = new AddressResultReceiver(new Handler());

        createTabs();
        Intent intent = getIntent();
        String intentAction = intent.getStringExtra("IntentAction");
        Log.e("LOB", "Main activity Intent action  "+intentAction);
        if(!oneTimeActivityStarted){
            oneTimeActivityStarted = true;

            checkPermissions();

           // Log.e("LOB", "Registered Address listner ");
           startService(new Intent(getApplicationContext(), LockService.class));//Power button service
        }
        currentAction = intentAction;
        Storage.storeinDB(Storage.currentAction, currentAction,this);


        if("SOSAlert".equals(intentAction) || "SaveLocation".equals(intentAction)){
            if("SaveLocation".equals(intentAction)){
                Storage.storeinDB(Storage.locationName, intent.getStringExtra(Storage.locationName),this);
            }

            Log.e("LOB", "Flag Danger ######### ");
            // First we need to check availability of play services
            if (checkPlayServices()) {
                Log.e("LOB", "buildGoogleApiClient");
                // Building the GoogleApi client
                buildGoogleApiClient();
                getUserLocation();
            }
        }


    }


    private void startAddressIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.LOCATION_DATA_EXTRA, mLastLocation);
        intent.putExtra(FetchAddressIntentService.ADDRESS_RESULT_RECEIVER, addressResultReceiver);

        startService(intent);
    }

    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)     != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,  Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.SEND_SMS},  MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)      != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)      != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
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
        Log.e("LOB", " getting location ");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
    private void displayLocation() {
        Log.e("LOB", " display location ");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        Log.e("LOB", " mLastLocation "+mLastLocation);
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            Log.e("LOB", "location ::::: "+latitude+ " --- "+longitude);
            startAddressIntentService();
        } else {

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


    private void createTabs(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Home"));
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


    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        Log.e("LOB", "Main activity got the result "+resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY));

    }
}
