package com.example.darragh.firstwearapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Darragh on 07/04/2016.
 */
public class ViewDB extends FragmentActivity{

    SQLiteDatabase db;
    ArrayList<String> results = new ArrayList<>();

    private ListView activityList;
    TextView text;
    String[] items = { "ID", "SPEED", "DIST", "TIME"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewdb_phone);

        activityList = (ListView) findViewById(R.id.activityListView);
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);

        activityList.setAdapter(aa);
        text = (TextView) findViewById(R.id.txtMsg);


        String SDcardPath = "data/data/cis470.matos.databases";
        String myDbPath = SDcardPath + "/" + "myActivity.db";

        //db = SQLiteDatabase.openDatabase(myDbPath, null,
                //SQLiteDatabase.OPEN_READONLY);

        try {
            Log.d("viewDB", "Pre SHOW Table");
            //showTable("tblActivity"); //retrieve all rows from a table
            //displayResultList();
            db.close(); // make sure to release the DB
        } catch (Exception e) {
            finish();
        }
    }

    private void showTable(String tableName) {

        try {
            Log.d("viewDB", "Try to show Table");
            String sel = "select * from " + tableName ;
            Cursor cursor = db.rawQuery(sel, null);

            if(cursor.moveToFirst()){
                Log.d("viewDB", "First");
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

        for(int i=0; i<results.size(); i++){

            Log.d("viewDB", results.get(i));
        }
    }
}
