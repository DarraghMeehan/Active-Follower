package com.example.darragh.firstwearapplication;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
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
import java.util.ArrayList;

public class FinishedActivity extends FragmentActivity {

    private TextView finalSpeed;
    private TextView finalDistance;
    private TextView finalTime;

    String speed;
    String distance;
    String time;
    ArrayList<Double> speedList = new ArrayList<>();

    SQLiteDatabase db;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
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
            finalSpeed.setText(speed + " km/h");

            double dist = extras.getDouble("distance");
            DecimalFormat distFormat = new DecimalFormat("##.##");
            distance = distFormat.format(dist);
            finalDistance.setText(distance + " km");

            time = extras.getString("time");
            finalTime.setText(time);

            speedPlot();
        }

        openDatabase(); // open (create if needed) database
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void speedPlot() {

        LineChart lineChart = (LineChart) findViewById(R.id.plot);
        speedList = (ArrayList<Double>) getIntent().getSerializableExtra("speedList");

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<String>();

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

    @Override
    public void onPause() {
        //dropTable();
        super.onPause();
    }

    public void onDestroy() {
        db.close();
        super.onDestroy();
    }

    private void openDatabase() {

        Log.d("FinishedActivity", "Open the Database");
        try {
            // path to private memory
            String SDcardPath = "data/data/com.example.darragh.firstwearapplication";
            String myDBPath = SDcardPath + "/" + "myActivity.db";
            Log.d("FinishedActivity", "DB Path: " + myDBPath);

            db = SQLiteDatabase.openDatabase(myDBPath, null,
                    SQLiteDatabase.CREATE_IF_NECESSARY);

        } catch (SQLiteException e) {
            finish();
        }
    }

    public void dropTable() {

        Log.d("FinishedActivity", "Drop the table");
        //db.beginTransaction();
        try {
            db.execSQL("drop table if exists tblActivity;");
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            finish();
        }
    }

    public void onClick_Save(View v) {

        Toast t = Toast.makeText(FinishedActivity.this, "Save to DB", Toast.LENGTH_SHORT);
        t.show();

        try {
            //dropTable(); // if needed drop table tblActivity
            insertData();
        } catch (Exception e) {
            finish();
        }
        //end();
    }

    public void onClick_View(View v) {

        Intent myIntent = new Intent(FinishedActivity.this, ViewDB.class);
        startActivity(myIntent);
    }

    private void insertData() {

        // create table: tblAmigo
        db.beginTransaction();
        try {
            Log.d("FinishedActivity", "Pre Table");
            // create table
            db.execSQL("create table if not exists tblActivity("
                    + " recID integer PRIMARY KEY autoincrement, "
                    + "speed  text, " + "distance text, " + "time text);");
            Log.d("FinishedActivity", "Post Table");
            // commit your changes
            db.setTransactionSuccessful();
        } catch (SQLiteException e1) {
            finish();
        } finally {
            db.endTransaction();
        }

        // populate table: tblAmigo
        db.beginTransaction();
        try {
            String myInfo = "insert into tblActivity(speed, distance, time) "
                    + " values ('" + speed + "', '" + distance + "', '" + time + "' );";
            db.execSQL(myInfo);

            // commit your changes
            db.setTransactionSuccessful();
        } catch (SQLiteException e2) {
            finish();
        } finally {
            db.endTransaction();
        }
    }

    public void end() {

        setContentView(R.layout.viewdb_phone);
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
