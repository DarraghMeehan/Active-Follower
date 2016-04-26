package com.example.darragh.firstwearapplication;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Darragh on 07/04/2016.
 */
public class ViewDB extends FragmentActivity{

    ListView activityList;
    ArrayList<String> returnedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewdb_phone);

        final MyDBHelper myDB = new MyDBHelper(this);
        activityList = (ListView) findViewById(R.id.activityListView);

        try {
            Log.d("viewDB", "Pre SHOW Table");
            returnedList = myDB.showTable(); //retrieve all rows from a table
            ArrayAdapter<String> myAdapter = new ArrayAdapter<>(this, R.layout.my_list, returnedList);
            activityList.setAdapter(myAdapter);
        }
        catch (Exception e) {
            finish();
        }
    }
}
