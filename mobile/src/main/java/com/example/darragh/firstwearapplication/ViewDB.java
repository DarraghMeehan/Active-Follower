package com.example.darragh.firstwearapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Darragh on 07/04/2016.
 */
public class ViewDB extends FragmentActivity{

    SQLiteDatabase db;
    private ListView activityList;
    ArrayList<String> results = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewdb_phone);

        activityList = (ListView) findViewById(R.id.activityListView);

        String SDcardPath = "data/data/cis470.matos.databases";
        String myDbPath = SDcardPath + "/" + "myActivity.db";

        db = SQLiteDatabase.openDatabase(myDbPath, null,
                SQLiteDatabase.OPEN_READONLY);

        try {
            Log.v("viewDB", "Pre SHOW Table");
            showTable("tblActivity"); //retrieve all rows from a table
            displayResultList();
            db.close(); // make sure to release the DB
        } catch (Exception e) {
            finish();
        }
    }

    private void showTable(String tableName) {

        try {
            Log.v("viewDB", "Try to show Table");
            String sel = "select * from " + tableName ;
            Cursor cursor = db.rawQuery(sel, null);

            if(cursor.moveToFirst()){
                Log.v("viewDB", "First");
                do {
                    //int index = cursor.getInt(cursor.getColumnIndex("recID"));
                    String speed = cursor.getString(cursor.getColumnIndex("speed"));
                    String distance = cursor.getString(cursor.getColumnIndex("distance"));
                    String time = cursor.getString(cursor.getColumnIndex("time"));
                    results.add("Speed: " + speed + "\nDistance: " + distance + "Time: " + time);
                }
                while(cursor.moveToNext());
            }
        }
        catch (Exception e) {

        }
    }

    private void displayResultList(){

        //activityList.setTe
    }
}
