package com.example.darragh.firstwearapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Darragh on 18/04/2016.
 */
public class MyDBHelper extends SQLiteOpenHelper {

    private static final String myDB = "activityDB";
    private static final int myVersion = 2;
    private static final String myDBTable = "myActivityTable";

    private static final String tblID = "recID";
    private static final String tblDATE = "Date";
    private static final String tblSPEED = "Speed";
    private static final String tblDISTANCE = "Distance";
    private static final String tblTIME = "Time";

    public static MyDBHelper instance;

    public MyDBHelper(Context context) {
        super(context, myDB, null, myVersion);
    }

    public static synchronized MyDBHelper getInstance(Context context) {

        if(instance == null)
            instance = new MyDBHelper(context.getApplicationContext());

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String myInfo = "create table if not exists " + myDBTable + "("
                + tblID + " integer PRIMARY KEY AUTOINCREMENT,"
                + tblDATE + " text,"
                + tblSPEED + " text,"
                + tblDISTANCE + " text,"
                + tblTIME + " text);";
        db.execSQL(myInfo);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if(oldVersion != newVersion){
            db.execSQL("DROP TABLE IF EXISTS" + myDBTable + ";");
            onCreate(db);
        }
    }

    public void insertData(String date, String speed, String distance, String time){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues myValues = new ContentValues();

        myValues.put(tblDATE, date);
        myValues.put(tblSPEED, speed);
        myValues.put(tblDISTANCE, distance);
        myValues.put(tblTIME, time);
        db.insert(myDBTable, null, myValues);
        db.close();
    }

    public ArrayList<String> showTable() {

        ArrayList<String> results = new ArrayList<>();
        String query = "SELECT Date, Speed, Distance, Time FROM " + myDBTable;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()){

            do{
                StringBuilder sb = new StringBuilder();
                sb.append(cursor.getString(0) + " ");
                sb.append(cursor.getString(1) + " ");
                sb.append(cursor.getString(2) + " ");
                sb.append(cursor.getString(3) + " ");

                results.add(sb.toString());
            }
            while(cursor.moveToNext());
        }
        return results;
    }
}
