package com.example.mark.pacmanroyale.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;

import com.example.mark.pacmanroyale.DrawingView;
import com.example.mark.pacmanroyale.R;


public class PlayActivity extends AppCompatActivity {

    private static final String TAG = "PlayActivity";
    static PlayActivity activity;
    private DrawingView drawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        activity = this;
        LinearLayout surfaceView = (LinearLayout)findViewById(R.id.middleSurface);
        drawingView = new DrawingView(this);
        surfaceView.addView(drawingView);
        
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        drawingView.resume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        drawingView.pause();
    }
}
