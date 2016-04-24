package com.example.darragh.firstwearapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Darragh on 30/03/2016.
 */
public class Home extends FragmentActivity {
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    TextView welcome;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_phone);

        welcome = (TextView) findViewById(R.id.welcome);
        welcome.setOnTouchListener(gestureListener);

        gestureDetector = new GestureDetector(new SwipeGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void onLeftSwipe() {

        Toast t = Toast.makeText(Home.this, "Gesture detected", Toast.LENGTH_SHORT);
        t.show();
        Intent myIntent = new Intent(Home.this, MapsActivity.class);
        startActivity(myIntent);
    }

    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 50;
        private static final int SWIPE_MAX_OFF_PATH = 200;
        private static final int SWIPE_THRESHOLD = 200;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            try {
                float diffAbs = Math.abs(e1.getY() - e2.getY());
                float diff = e1.getX() - e2.getX();

                if (diffAbs > SWIPE_MAX_OFF_PATH)
                    return false;

                // Left swipe
                if (diff > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD) {
                    Home.this.onLeftSwipe();
                    Log.e("Home", "Left detected");
                }
            }
            catch (Exception e) {
                Log.e("Home", "Error on gestures");
            }
            return false;
        }
    }
}