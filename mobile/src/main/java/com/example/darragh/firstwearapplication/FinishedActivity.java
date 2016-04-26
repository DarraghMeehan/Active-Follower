package com.example.darragh.firstwearapplication;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FinishedActivity extends FragmentActivity {

    private TextView finalSpeed;
    private TextView finalDistance;
    private TextView finalTime;
    private boolean isSaved = false;

    String speed;
    String distance;
    String time;
    ArrayList<Double> speedList = new ArrayList<>();

    final MyDBHelper myDB = new MyDBHelper(this);
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finished_phone);

        finalSpeed = (TextView) findViewById(R.id.speed);
        finalDistance = (TextView) findViewById(R.id.distance);
        finalTime = (TextView) findViewById(R.id.time);

        //Read the data passed from Phone Activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            double spd = extras.getDouble("speed");
            DecimalFormat speedFormat = new DecimalFormat("##.##");
            speed = speedFormat.format(spd);
            finalSpeed.setText("Avg. Speed: ");
            finalSpeed.append(speed + " km/h");

            double dist = extras.getDouble("distance");
            DecimalFormat distFormat = new DecimalFormat("##.##");
            distance = distFormat.format(dist);
            finalDistance.setText("Distance: ");
            finalDistance.append(distance + " km");

            time = extras.getString("time");
            finalTime.setText("Time: ");
            finalTime.append(time); 

            speedPlot();
        }
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void speedPlot() {

        LineChart lineChart = (LineChart) findViewById(R.id.chart);
        speedList = (ArrayList<Double>) getIntent().getSerializableExtra("speedList");

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for(int i=0;i<speedList.size();i++) {
            entries.add(new Entry(speedList.get(i).floatValue(), i));
            labels.add("label " + i);
        }
        LineDataSet dataset = new LineDataSet(entries, "Speed");
        dataset.setColor(Color.MAGENTA);
        dataset.setDrawFilled(true);

        LineData data = new LineData(labels, dataset);
        lineChart.setData(data); // set the data and list of lables into chart
        lineChart.setDescription("Speed data");  // set the description
    }

    public void onClick_Save(View v) {

        if(isSaved == false) {

            Toast toast = Toast.makeText(FinishedActivity.this, "Your activity has been saved!", Toast.LENGTH_SHORT);
            toast.show();

            String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            myDB.insertData(date, speed, distance, time);

            isSaved=true;
        }
        else {
            Toast saved = Toast.makeText(FinishedActivity.this, "You have already saved this activity!", Toast.LENGTH_LONG);
            saved.show();
        }
    }

    public void onClick_View(View v) {

        Intent myIntent = new Intent(FinishedActivity.this, ViewDB.class);
        startActivity(myIntent);
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Finished Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.darragh.firstwearapplication/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Finished Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.darragh.firstwearapplication/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
