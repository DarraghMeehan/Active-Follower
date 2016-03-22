package com.example.darragh.firstwearapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wearable.Wearable;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class WatchActivity extends WearableActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener  {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.UK);

    //Location Variables
    private double longitude;
    private double latitude;
    LatLng startLocation;
    //Tracking user location and printing the route
    List<LatLng> routePoints = new ArrayList<>();
    private double totalDistance;

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView speed;
    private TextView distance;
    private TextView runTime;
    private TextView mClockView;
    double mySpeed;
    GoogleApiClient googleApiClient;

    Chronometer myChrono;
    long timeWhenPaused = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                //mTextView = (TextView) stub.findViewById(R.id.text);
                speed = (TextView) stub.findViewById(R.id.speed);
                distance = (TextView) stub.findViewById(R.id.distance);
                //runTime = (TextView) stub.findViewById(R.id.runTime);
                myChrono= (Chronometer) findViewById(R.id.myChrono);
                //distance.setText("nil");
            }
        });
        setAmbientEnabled();

        // Register the local broadcast receiver
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("message");
            // Display message in UI
            //distance.setText(message);


            if(message.equals("Start")){
                myChrono.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
                myChrono.start();
                distance.setText(message);
            }
            else if(message.equals("Wow")) {
                timeWhenPaused = myChrono.getBase() - SystemClock.elapsedRealtime();
                myChrono.stop();
                distance.setText(message);
            }
        }
    }

    // Connect to Google Play Services when the Activity starts
    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    // Disconnect from Google Play Services when the Activity stops
    @Override
    protected void onStop() {

        if (googleApiClient != null){
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    // Register as a listener when connected
    @Override
    public void onConnected(Bundle connectionHint) {

        Toast.makeText(getBaseContext(), "Connected to phone ", Toast.LENGTH_LONG).show();
        //runTime.setText("Time 0");
        LocationRequest locationRequest = LocationRequest.create(); // Create the LocationRequest object
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Use high accuracy
        locationRequest.setInterval(TimeUnit.SECONDS.toMillis(2)); // Set the update interval to 2 seconds
        locationRequest.setFastestInterval(TimeUnit.SECONDS.toMillis(2)); // Set the fastest update interval to 2 seconds
        locationRequest.setSmallestDisplacement(2); // Set the minimum displacement
        // Register listener using the LocationRequest object
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        startLocation = new LatLng(latitude, longitude);
        routePoints.add(startLocation);
    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        //retryConnecting();
    }

    @Override
    public void onLocationChanged(Location location){

        // Display the latitude and longitude in the UI
        mySpeed = location.getSpeed() * 3.6;
        DecimalFormat formatter = new DecimalFormat("##.##");
        String s = formatter.format(mySpeed);
        //speed.setText("Latitude:  " + String.valueOf(location.getLatitude()) +
           //     "\nLongitude:  " + String.valueOf(location.getLongitude()));
        speed.setText(s + "km/h");

        //Get current position
        LatLng mapPoint = new LatLng(latitude, longitude);
        //Add current position to ArrayList
        routePoints.add(mapPoint);
        //totalDistance = mapPoint.distanceTo(startLocation);

        // Loop for the size of the ArrayList
        for (int z = 0; z < routePoints.size(); z++) {
            LatLng point = routePoints.get(z);

           // totalDistance = totalDistance + point;
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mTextView.setTextColor(getResources().getColor(android.R.color.white));
            speed.setTextColor(getResources().getColor(android.R.color.white));
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            mTextView.setTextColor(getResources().getColor(android.R.color.black));
            speed.setTextColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.GONE);
        }
    }
}
