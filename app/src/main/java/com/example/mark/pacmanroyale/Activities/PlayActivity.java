package com.example.mark.pacmanroyale.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;

import com.example.mark.pacmanroyale.DrawingView;
import com.example.mark.pacmanroyale.R;


public class PlayActivity extends AppCompatActivity {

    private static final String TAG = "PlayActivity";
    private static final String GAME_MODE = "GAME_MODE";
    static PlayActivity activity;
    private DrawingView drawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        activity = this;
        LinearLayout surfaceView = findViewById(R.id.middleSurface);
        drawingView = new DrawingView(this, getIntent().getBooleanExtra(GAME_MODE, false));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Utils.setUserPresenceOffline(this);
    }
}
