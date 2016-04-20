package com.example.darragh.firstwearapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Darragh on 07/04/2016.
 */
public class ViewDB extends FragmentActivity{

    private static final String myDBTable = "myActivityTable";

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
            final ArrayAdapter<String> myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, returnedList);
            activityList.setAdapter(myAdapter);

            activityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> a, View v, int position, long id) {

                    AlertDialog.Builder adb = new AlertDialog.Builder(ViewDB.this);

                    adb.setTitle("Delete?");
                    adb.setMessage("Are you sure you want to delete " + position);
                    final int positionToRemove = position + 1;
                    adb.setNegativeButton("Cancel", null);

                    adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            myDB.deleteEntry(positionToRemove);
                            Toast t = Toast.makeText(ViewDB.this, "Value of Position: " + positionToRemove, Toast.LENGTH_LONG);
                            t.show();
                            myAdapter.notifyDataSetChanged();
                        }
                    });
                    adb.show();
                }
            });
        }
        catch (Exception e) {
            finish();
        }
    }
}
