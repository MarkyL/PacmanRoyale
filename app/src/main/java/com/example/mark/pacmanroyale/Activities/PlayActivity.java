package com.example.mark.pacmanroyale.Activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mark.pacmanroyale.DrawingView;
import com.example.mark.pacmanroyale.Enums.GameMode;
import com.example.mark.pacmanroyale.R;
import com.example.mark.pacmanroyale.Utils;


public class PlayActivity extends AppCompatActivity implements DrawingView.Iinterface{

    private static final String TAG = "PlayActivity";
    private static final String GAME_MODE = "GAME_MODE";
    private static final int DEFAULT_PACMAN_VISIBILTY = 255;
    private static PlayActivity activity;
    private DrawingView drawingView;
    private ImageView loaderImage;
    private Button invisibleButton;
    private TextView invisibleTime;
    private GameMode gameMode;


    private LinearLayout surfaceView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        activity = this;
        loaderImage = findViewById(R.id.play_loader);
        surfaceView = findViewById(R.id.middleSurface);
        invisibleButton = findViewById(R.id.GoInvisible);
        invisibleTime = findViewById(R.id.invisibleTime);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        if (drawingView == null) { // meaning we are not initialized yet - let's initialize then.
            gameMode = (GameMode) getIntent().getSerializableExtra(GAME_MODE);
            drawingView = new DrawingView(this, (GameMode) getIntent().getSerializableExtra(GAME_MODE));
            surfaceView.addView(drawingView);
            drawingView.setCanDrawState(true);
        }
        drawingView.resume();
    }

    public void setVisibilities() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3500);
                } catch (InterruptedException e) {
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "setVisibilities() setting visibilities");
                        loaderImage.setVisibility(View.INVISIBLE);
                        surfaceView.setVisibility(View.VISIBLE);
                        if(gameMode != GameMode.GHOST) {
                            invisibleButton.setVisibility(View.VISIBLE);
                            invisibleTime.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        };
        thread.start();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        drawingView.pause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: PlayActivity called");
        if (Utils.getVirtualRoomReference() != null) {
            Utils.getVirtualRoomReference().removeValue();
        }
        super.onDestroy();
        //Utils.setUserPresenceOffline(this);
    }


    public void goInvisible(View view) {

       drawingView.goInvisible(125);
       invisibleButton.setEnabled(false);

       new CountDownTimer(3000,100){
           @Override
           public void onTick(long l) {
               int seconds = (int) (l/1000);
               int milliSeconds = (int) (l % 1000)/100;
               String timeLeft = String.valueOf(seconds) + "." + String.valueOf(milliSeconds);
               invisibleTime.setText(timeLeft);
           }

           @Override
           public void onFinish() {
               invisibleTime.setText("");
           }

       }.start();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawingView.goInvisible(DEFAULT_PACMAN_VISIBILTY);
            }
        }, 3000);


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                invisibleButton.setEnabled(true);
            }
        },9000);

    }
}
