package com.example.darragh.firstwearapplication;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.XYPlot;

import java.text.DecimalFormat;


public class FinishedActivity extends FragmentActivity {

    private TextView finalSpeed;
    private TextView finalDistance;
    private TextView finalTime;

    private XYPlot plot;

    String speed;
    String distance;
    String time;
    //private double[] speedList = new double[];
    //double[] speedList = new double[a];

    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finished_phone);

        plot = (XYPlot) findViewById(R.id.plot);

        finalSpeed = (TextView) findViewById(R.id.speed);
        finalDistance = (TextView) findViewById(R.id.distance);
        finalTime = (TextView) findViewById(R.id.time);

        //Read the data passed from Phone Activity
        Bundle extras = getIntent().getExtras();
        if(extras != null) {

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

            //speedList = extras.getDoubleArray("speedList");
            convertArray();

            //XYSeries series1 = new SimpleXYSeries(Arrays.<double[]>asList(list),
                    //SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");
        }

        openDatabase(); // open (create if needed) database
    }

    private void convertArray() {

        //double[] list = new double[]
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

        }
        catch (SQLiteException e) {
            finish();
        }
    }

    public void dropTable() {

        Log.d("FinishedActivity", "Drop the table");
        //db.beginTransaction();
        try {
            db.execSQL("drop table if exists tblActivity;");
            //db.setTransactionSuccessful();
        }
        catch (Exception e) {
            finish();
        }
    }

    public void onClick_Save(View v) {

        Toast t = Toast.makeText(FinishedActivity.this, "Save to DB", Toast.LENGTH_SHORT);
        t.show();

        try{
            //dropTable(); // if needed drop table tblActivity
            insertData();
        }
        catch(Exception e){
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
        }
        catch (SQLiteException e1) {
            finish();
        }
        finally {
            db.endTransaction();
        }

        // populate table: tblAmigo
        db.beginTransaction();
        try {
            String myInfo = "insert into tblActivity(speed, distance, time) "
                    + " values ('"+speed+"', '"+distance+"', '"+time+"' );";
            db.execSQL(myInfo);

            // commit your changes
            db.setTransactionSuccessful();
        }
        catch (SQLiteException e2) {
            finish();
        }
        finally {
            db.endTransaction();
        }
    }

    public void end(){

        setContentView(R.layout.viewdb_phone);
    }
}
