package com.example.darragh.firstwearapplication;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;


public class FinishedActivity extends FragmentActivity {

    private TextView finalSpeed;
    private TextView finalDistance;
    private TextView finalTime;

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

            double speed = extras.getDouble("speed");
            DecimalFormat speedFormat = new DecimalFormat("##.##");
            String s = speedFormat.format(speed);
            finalSpeed.setText(s + " km/h");

            double dist = extras.getDouble("distance");
            DecimalFormat distFormat = new DecimalFormat("##.##");
            String d = distFormat.format(dist);
            finalDistance.setText(d + " km");

            String time = extras.getString("time");
            finalTime.setText(time);
        }
    }

    public void onClick_Save(View v) {

        Toast t = Toast.makeText(FinishedActivity.this, "Save to DB", Toast.LENGTH_SHORT);
        t.show();
    }
}
