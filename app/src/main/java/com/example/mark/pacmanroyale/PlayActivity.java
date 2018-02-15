package com.example.mark.pacmanroyale;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


public class PlayActivity extends AppCompatActivity {

    private static final String TAG = "PlayActivity";
    static PlayActivity activity;
    private DrawingView drawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawingView = new DrawingView(this);
        setContentView(drawingView); //(R.layout.activity_play);
//        activity = this;
//        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
//        surfaceView=drawingView;
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
