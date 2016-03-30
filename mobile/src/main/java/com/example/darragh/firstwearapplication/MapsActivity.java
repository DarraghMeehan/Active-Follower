package com.example.darragh.firstwearapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.BLUE;

public class MapsActivity extends Home implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    //Location Variables
    private double longitude;
    private double latitude;
    final Criteria criteria = new Criteria();

    private TextView speed;
    private TextView distance;
    double mySpeed;
    double totalDistance;
    ArrayList<Location> locations = new ArrayList<>();

    // Stopwatch features
    Chronometer myChrono;
    long timeWhenPaused = 0;
    boolean status = false;
    Button startButton;

    //Map & map manipulation
    private GoogleMap map;
    private LocationManager locationManager;
    private LocationListener locationListener;

    //Tracking user location and printing the route
    List<LatLng> routePoints = new ArrayList<>();
    Polyline myRoute;

    GoogleApiClient googleClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        // Build a new GoogleApiClient that includes the Wearable API
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        startButton = (Button) findViewById(R.id.btnStart);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        speed = (TextView) findViewById(R.id.speed);
        speed.setText(0 + " km/h");
        distance = (TextView) findViewById(R.id.distance);
        distance.setText(0 + " km");
        myChrono = (Chronometer) findViewById(R.id.chronometer);
        myChrono.setText("00:00");

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

                PolylineOptions options = new PolylineOptions()
                        .width(5)
                        .color(BLUE)
                        .geodesic(true);

                for (int z = 0; z < routePoints.size(); z++) {
                    LatLng point = routePoints.get(z);
                    options.add(point);
                }
                myRoute = map.addPolyline(options);

                mySpeed = location.getSpeed() * 3.6;
                DecimalFormat speedFormat = new DecimalFormat("##.##");
                String s = speedFormat.format(mySpeed);
                speed.setText(s + "km/h");

                Location current = new Location("Current");
                current.setLatitude(location.getLatitude());
                current.setLongitude(location.getLongitude());
                locations.add(current);

                for(int i = 0; i < locations.size(); i++){

                    if(i==0);
                    else{
                        Location previous = locations.get(i - 1);
                        Location next = locations.get(i);
                        totalDistance = totalDistance + next.distanceTo(previous) / 1000;
                        DecimalFormat distFormat = new DecimalFormat("##.##");
                        String d = distFormat.format(totalDistance);
                        distance.setText(d + " km");
                    }
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{

                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
            }
            return;
        }
        locationManager.requestLocationUpdates("gps", 5000, 5, locationListener);
        String provider = locationManager.getBestProvider(criteria, true);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
    }

    // Connect to the data layer when the Activity starts
    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
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
        if (status)
            pause();
        else
            play();
        status = !status;

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng MY_LOCATION = new LatLng(latitude, longitude);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(MY_LOCATION, 17);

        map.addMarker(new MarkerOptions().position(MY_LOCATION)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        map.animateCamera(update);

    }

    public void pause(){

        //Changes the text and the colour of the button
        startButton.setText("Resume");
        startButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);

        //Send message to watch
        String message = "Pause";
        new SendToDataLayerThread("/message_path", message).start();

        //Save the current time
        timeWhenPaused = myChrono.getBase() - SystemClock.elapsedRealtime();
        myChrono.stop();
    }

    public void play(){

        //Changes the text and the colour of the button
        startButton.setText("Pause");
        startButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);

        //Send message to watch
        String message = "Start";
        new SendToDataLayerThread("/message_path", message).start();

        //Resume time count from previous time.
        myChrono.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
        myChrono.start();
    }

    public void onClick_Track(View v) {

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng MY_LOCATION = new LatLng(latitude, longitude);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(MY_LOCATION, 17);

        map.addMarker(new MarkerOptions().position(MY_LOCATION)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        map.animateCamera(update);
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
