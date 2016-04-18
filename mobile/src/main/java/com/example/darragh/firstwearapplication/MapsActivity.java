package com.example.darragh.firstwearapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.BLUE;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    //Location Variables
    private double longitude;
    private double latitude;
    double latitude_prev = 0;
    double longitude_prev = 0;

    //Distance Variables
    static double totalDist = 0;
    double totalDistance;
    private TextView distance;

    //Speed Variables
    private TextView speed;
    double mySpeed;
    ArrayList<Double> speedList = new ArrayList<>();
    double[] speedArray;

    //Time Features
    Chronometer myChrono;
    long timeWhenPaused = 0;
    boolean status = false;

    //Buttons
    Button startButton;
    Button finishButton;

    //Map & Map Manipulation
    private GoogleMap map;
    private LocationManager locationManager;
    private LocationListener locationListener;

    //Tracking user location
    List<LatLng> routePoints = new ArrayList<>();
    private List<Marker> markerList = new ArrayList<>();
    PolylineOptions options = new PolylineOptions()
            .width(10)
            .color(BLUE)
            .geodesic(true);
    GoogleApiClient googleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        // Build a new GoogleApiClient that includes the Wearable API
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API).build();

        startButton = (Button) findViewById(R.id.btnStart);
        finishButton = (Button) findViewById(R.id.btnFinish);
        //Hide the finish button
        finishButton.setVisibility(View.INVISIBLE);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        //Initialise values of text areas
        speed = (TextView) findViewById(R.id.speed);
        speed.setText(0 + " km/h");
        distance = (TextView) findViewById(R.id.distance);
        distance.setText(0 + " km");
        myChrono = (Chronometer) findViewById(R.id.chronometer);
        myChrono.setText("00:00");

        //Send message to Watch
        String message = "Create";
        new SendToDataLayerThread("/message_path", message).start();

        //Calling the Location Service
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                //Update Location variables
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                LatLng mapPoint = new LatLng(latitude, longitude);
                routePoints.add(mapPoint);

                if (status) {
                    //Take location & read speed info
                    getSpeed(location);
                    //Take locations array & read distance info
                    getLocation(location);
                } else ;
            }

            private void getLocation(Location location) {

                double lat = location.getLatitude();
                double lon = location.getLongitude();
                LatLng current = new LatLng(lat, lon);

                if (latitude_prev == 0 && longitude_prev == 0) {
                    latitude_prev = lat;
                    longitude_prev = lon;
                    totalDistance = 0;
                }
                else {
                    //Get the distance covered from point A to point B
                    totalDistance = getDistance(latitude_prev, longitude_prev, lat, lon);

                    //Set previous latitude and longitude to the last location
                    latitude_prev = lat;
                    longitude_prev = lon;

                    //Print the route line on the map
                    options.add(current);
                    map.addPolyline(options);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(current, 17);
                    map.moveCamera(cameraUpdate);

                    //Print the distance information
                    DecimalFormat distFormat = new DecimalFormat("##.##");
                    String d = distFormat.format(totalDistance);
                    distance.setText(d + " km");
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {

                Toast.makeText(getBaseContext(), "GPS turned on ", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProviderDisabled(String provider) {

                //Enable Location service via an intent
                Toast.makeText(getBaseContext(), "Activating Location Services ", Toast.LENGTH_LONG).show();
                Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(locationIntent);
            }
        };
        //Needed for Google maps use
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{

                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
            }
            return;
        }
        //To allow the use of Google maps
        locationManager.requestLocationUpdates("gps", 500, 1, locationListener);

        // Register the local broadcast receiver
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
    }

    //Reading messages coming from the watch
    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("message");
            // Display message in UI

            if(message.equals("Start")){
                play();
                status = !status;
            }
            else if(message.equals("Pause")) {
                pause();
                status = !status;
            }
        }
    }

    //Returns the distance of the activity
    private static Double getDistance(double lat1, double lng1, double lat2, double lng2){

        double earthRadius = 6371;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance =  earthRadius * c;

        totalDist = totalDist + distance;
        return totalDist;
    }

    //Returns the speed of the user
    private void getSpeed(Location location) {

        mySpeed = location.getSpeed() * 3.6;
        speedList.add(mySpeed); //Add current speed to the arraylist
        DecimalFormat speedFormat = new DecimalFormat("##.##");
        String s = speedFormat.format(mySpeed);
        speed.setText(s + "km/h");
    }

    // Connect to the data layer when the Activity starts
    @Override
    protected void onStart() {

        super.onStart();
        googleClient.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.darragh.firstwearapplication/http/host/path")
        );
        AppIndex.AppIndexApi.start(googleClient, viewAction);
    }

    // Send a message when the data layer connection is successful.
    @Override
    public void onConnected(Bundle connectionHint) {

        String message = "Hello wearable\n Via the data layer";
        //Requires a new thread to avoid blocking the UI
        new SendToDataLayerThread("/message_path", message).start();
    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {

        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.darragh.firstwearapplication/http/host/path")
        );
        AppIndex.AppIndexApi.end(googleClient, viewAction);
    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //Send messages to the watch
    class SendToDataLayerThread extends Thread {

        String path;
        String message;

        // Constructor to send a message to the data layer
        SendToDataLayerThread(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {

            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), path, message.getBytes()).await();
                if (result.getStatus().isSuccess()) {
                    Log.v("myTag", "Message: {" + message + "} sent to: " + node.getDisplayName());
                } else {
                    // Log an error
                    Log.v("myTag", "ERROR: failed to send Message");
                }
            }
        }
    }

    public void onClick_Start(View v) {

        //Changes the status of the button
        if (status) {
            pause();
            //Send message to watch
            String message = "Pause";
            new SendToDataLayerThread("/message_path", message).start();
        }
        else {
            play();
            //Send message to watch
            String message = "Start";
            new SendToDataLayerThread("/message_path", message).start();
        }
        //Toggle the status of the stopwatch
        status = !status;

        //Drop Yellow marker at paused position
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng pauseLocation = new LatLng(latitude, longitude);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(pauseLocation, 17);
        map.addMarker(new MarkerOptions().position(pauseLocation)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        map.animateCamera(update);
    }

    public void pause(){

        //Show the finish button
        finishButton.setVisibility(View.VISIBLE);

        //Changes the text and the colour of the button
        startButton.setText("Resume");
        startButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);

        //Save the current time
        timeWhenPaused = myChrono.getBase() - SystemClock.elapsedRealtime();
        myChrono.stop();

        int last = routePoints.size();
        if(last>=2){

            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            boundsBuilder.include(routePoints.get(0));
            boundsBuilder.include(routePoints.get(last-1));

            LatLngBounds bounds = boundsBuilder.build();
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,0));
        }
        else;
    }

    public void play(){

        //Hide the finish button
        finishButton.setVisibility(View.INVISIBLE);

        //Changes the text and the colour of the button
        startButton.setText("Pause");
        startButton.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);

        //Resume time count from previous time.
        myChrono.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
        myChrono.start();
    }

    public void onClick_Finish(View v) {

        //Send message to Watch
        String message = "Finish";
        new SendToDataLayerThread("/message_path", message).start();

        //Stop the Stopwatch
        myChrono.stop();
        String finalText = myChrono.getText().toString();
        final double finalDist = totalDist;

        double avgSpeed = calculateAverageSpeed();

        //Start the final Activity
        Intent intentFinished = new Intent(MapsActivity.this, FinishedActivity.class);
        intentFinished.putExtra("speed", avgSpeed);
        intentFinished.putExtra("time", finalText);
        intentFinished.putExtra("distance", finalDist);
        intentFinished.putExtra("speedList", speedList);
        startActivity(intentFinished);
    }

    private double calculateAverageSpeed() {

        double total = 0;
        for(int i = 0; i<speedList.size(); i++){

            total += speedList.get(i);
        }
        double average = total/speedList.size();
        return(average);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}