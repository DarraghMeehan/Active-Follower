package com.example.darragh.firstwearapplication;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;


public class FinishedActivity extends FragmentActivity {

    private TextView finalSpeed;
    private TextView finalDistance;
    private TextView finalTime;

    String speed;
    String distance;
    String time;

    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finished_phone);

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
        }
    }

    @Override
    public void onPause() {
        dropTable();
        super.onPause();
    }

    public void onDestroy() {
        db.close();
        super.onDestroy();
    }

    private void openDatabase() {

        Log.i("FinishedActivity", "Open the Database");
        try {
            // path to private memory
            String SDcardPath = "data/data/com.example.darragh.firstwearapplication";
            String myDBPath = SDcardPath + "/" + "myActivity.db";
            Log.i("FinishedActivity", "DB Path: " + myDBPath);

            db = SQLiteDatabase.openDatabase(myDBPath, null,
                    SQLiteDatabase.CREATE_IF_NECESSARY);

        }
        catch (SQLiteException e) {
            finish();
        }
    }

    public void dropTable() {

        Log.i("FinishedActivity", "Drop the table");
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

            openDatabase(); // open (create if needed) database
            dropTable(); // if needed drop table tblActivity
            insertData();
        }
        catch(Exception e){
            finish();
        }

        end();
    }

    private void insertData() {

        // create table: tblAmigo
        db.beginTransaction();
        try {
            Log.i("FinishedActivity", "Pre Table");
            // create table
            db.execSQL("create table tblActivity ("
                    + " recID integer PRIMARY KEY autoincrement, "
                    + "speed  text, " + "distance text, " + "time text);");
            Log.i("FinishedActivity", "Post Table");
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
            //finish();
        }
        finally {
            db.endTransaction();
        }
    }

    public void end(){

        setContentView(R.layout.viewdb_phone);
    }
}
