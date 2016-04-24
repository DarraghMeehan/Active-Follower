package com.example.darragh.firstwearapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class WatchActivity extends WearableActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener  {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.UK);

    //Location Variables
    //Tracking user location and printing the route
    double latitude_prev = 0;
    double longitude_prev = 0;

    //Distance Variables
    static double totalDist = 0;
    double totalDistance = 0;

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;
    private TextView speed;
    double mySpeed;
    private TextView distance;

    GoogleApiClient googleApiClient;

    // Stopwatch features
    Chronometer myChrono;
    long timeWhenPaused = 0;
    boolean status = false;

    Button activityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                //mTextView = (TextView) stub.findViewById(R.id.text);
                speed = (TextView) stub.findViewById(R.id.speed);
                speed.setText(0 + "\nkm/h");
                distance = (TextView) stub.findViewById(R.id.distance);
                distance.setText(0 + "\nkm");
                myChrono = (Chronometer) findViewById(R.id.myChrono);
                myChrono.setText("00:00");

                activityButton = (Button) findViewById(R.id.btnWatch);
                activityButton.setVisibility(View.INVISIBLE);

            }
        });
        setAmbientEnabled();

        // Register the local broadcast receiver
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
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
        googleApiClient.connect();
    }

    // Disconnect from Google Play Services when the Activity stops
    @Override
    protected void onStop() {

        if (googleApiClient != null){
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("message");

            if(message.equals("Start")){
                //Show the button
                activityButton.setVisibility(View.VISIBLE);
                play();
                status = !status;
            }
            else if(message.equals("Pause")) {
                pause();
                status = !status;
            }
            else if(message.equals("Finish")) {
                end();
            }
        }
    }

    // Register as a listener when connected
    @Override
    public void onConnected(Bundle connectionHint) {

        String message = "Hello wearable\n Via the data layer";
        //Requires a new thread to avoid blocking the UI
        new SendToDataLayerThread("/message_path", message).start();

        LocationRequest locationRequest = LocationRequest.create(); // Create the LocationRequest object
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Use high accuracy
        locationRequest.setInterval(TimeUnit.SECONDS.toMillis(2)); // Set the update interval to 2 seconds
        locationRequest.setFastestInterval(TimeUnit.SECONDS.toMillis(2)); // Set the fastest update interval to 2 seconds
        locationRequest.setSmallestDisplacement(2); // Set the minimum displacement
        // Register listener using the LocationRequest object
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        //retryConnecting();
    }

    @Override
    public void onLocationChanged(Location location) {

        if (status) {

            //Take location & read speed info
            getSpeed(location);

            //Take locations array & read distance info
            getLocation(location);
        } else ;
    }

    private void getLocation(Location location){

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        if(latitude_prev==0 && longitude_prev==0){
            latitude_prev = lat;
            longitude_prev = lon;
            totalDistance = 0;
        }
        else{
            //Get the distance covered from point A to point B
            totalDistance = getDistance(latitude_prev, longitude_prev, lat, lon);

            //Set previous latitude and longitude to the last location
            latitude_prev = lat;
            longitude_prev = lon;

            //Print the distance information
            DecimalFormat distFormat = new DecimalFormat("##.##");
            String d = distFormat.format(totalDistance);
            distance.setText(d + " km");
        }
    }

    private static Double getDistance(double lat1, double lng1, double lat2, double lng2){

        double earthRadius = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance =  earthRadius * c;

        totalDist = totalDist + distance;
        return totalDist;
    }

    private void getSpeed(Location location) {

        mySpeed = location.getSpeed() * 3.6;
        DecimalFormat speedFormat = new DecimalFormat("##.##");
        String s = speedFormat.format(mySpeed);
        speed.setText(s + "km/h");
    }

    class SendToDataLayerThread extends Thread {

        String path;
        String message;

        // Constructor to send a message to the data layer
        SendToDataLayerThread(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {

            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result =
                        Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path, message.getBytes()).await();
                if (result.getStatus().isSuccess()) {
                    Log.v("Watch", "Message: {" + message + "} sent to: " + node.getDisplayName());
                } else {
                    // Log an error
                    Log.v("Watch", "ERROR: failed to send Message");
                }
            }
        }
    }

    public void onClick_Watch(View v) {

        //Changes the status of the button
        if (status) {
            pause();
            //Send message to Phone
            String message = "Pause";
            new SendToDataLayerThread("/message_path", message).start();
        }
        else {
            play();
            //Send message to Phone
            String message = "Start";
            new SendToDataLayerThread("/message_path", message).start();
        }
        //Toggle the status of the stopwatch
        status = !status;
    }

    public void pause(){

        //Changes the text and the colour of the button
        activityButton.setText("Resume");
        activityButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);

        //Save the current time
        timeWhenPaused = myChrono.getBase() - SystemClock.elapsedRealtime();
        myChrono.stop();
    }

    public void play(){

        //Changes the text and the colour of the button
        activityButton.setText("Pause");
        activityButton.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);

        //Resume time count from previous time.
        myChrono.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
        myChrono.start();
    }

    public void end(){

        setContentView(R.layout.finished_watch);
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

    public void onDestroy() {

        super.onDestroy();
    }
}
