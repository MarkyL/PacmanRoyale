package com.example.mark.pacmanroyale.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mark.pacmanroyale.DrawingView;
import com.example.mark.pacmanroyale.Enums.GameMode;
import com.example.mark.pacmanroyale.R;
import com.example.mark.pacmanroyale.Utilities.FireBaseUtils;
import com.example.mark.pacmanroyale.Utilities.UserInformationUtils;
import com.example.mark.pacmanroyale.Utilities.VirtualRoomUtils;

import io.github.controlwear.virtual.joystick.android.JoystickView;


public class PlayActivity extends AppCompatActivity implements DrawingView.Iinterface, View.OnClickListener{

    private static final String TAG = "PlayActivity";
    private static final String GAME_MODE = "GAME_MODE";
    private static final int DEFAULT_PACMAN_VISIBILTY = 255;
    private static PlayActivity activity;
    private DrawingView drawingView;
    private ImageView loaderImage;
    private Button invisibleButton;
    private TextView invisibleTime;
    private TextView percentageTV;
    private GameMode gameMode;

    float x1, y1, x2, y2;

    private LinearLayout surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        activity = this;
        initUI();
    }

    private void initUI() {
        loaderImage = findViewById(R.id.play_loader);
        surfaceView = findViewById(R.id.middleSurface);
        percentageTV = findViewById(R.id.percentageTV);
        invisibleButton = findViewById(R.id.goInvisible);
        invisibleTime = findViewById(R.id.invisibleTime);
        invisibleButton.setOnClickListener(this);
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
        if (gameMode == GameMode.VS_PC) {
            loaderImage.setVisibility(View.INVISIBLE);
        }
        initJoyStick();
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
                        //if (gameMode != GameMode.GHOST) {
                            invisibleButton.setVisibility(View.VISIBLE);
                            invisibleTime.setVisibility(View.VISIBLE);
                        //}
                    }
                });
            }
        };
        thread.start();
    }

    @Override
    public void endGame(final boolean isPacman) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(PlayActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.endgame_dialog, null);
                TextView endGameMsg = mView.findViewById(R.id.endGameMsg);
                Button endGameButton = mView.findViewById(R.id.endGameButton);

                String winMsg = getString(R.string.game_won);
                String loseMsg = getString(R.string.game_lost);

                if(isPacman) {
                    if(gameMode == GameMode.PACMAN)
                        endGameMsg.setText(winMsg);
                    else if(gameMode == GameMode.GHOST){
                        endGameMsg.setText(loseMsg);
                    }
                    else
                        endGameMsg.setText(winMsg);

                    UserInformationUtils.setUserPresenceOnline(PlayActivity.this);
                    if(gameMode != GameMode.VS_PC) {
                        VirtualRoomUtils.setVirtualGameRoom(null);
                        FireBaseUtils.getFireBaseVirtualRoomReference(PlayActivity.this).removeValue();
                    }
                }
                else{
                    if(gameMode == GameMode.PACMAN)
                        endGameMsg.setText(loseMsg);
                    else if(gameMode == GameMode.GHOST){
                        endGameMsg.setText(winMsg);
                    }
                    else
                        endGameMsg.setText(loseMsg);

                    UserInformationUtils.setUserPresenceOnline(PlayActivity.this);
                    if(gameMode != GameMode.VS_PC) {
                        VirtualRoomUtils.setVirtualGameRoom(null);
                        FireBaseUtils.getFireBaseVirtualRoomReference(PlayActivity.this).removeValue();
                    }
                }

                endGameButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PlayActivity.super.onBackPressed();
                    }
                });

                mBuilder.setView(mView);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
        });
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
        if (VirtualRoomUtils.getVirtualRoomReference() != null) {
            VirtualRoomUtils.getVirtualRoomReference().removeValue();
        }
        super.onDestroy();
        //FireBaseUtils.setUserPresenceOffline(this);
    }


    public void goInvisible() {
        if (gameMode == GameMode.PACMAN) {
            drawingView.goInvisible(125);
            invisibleButton.setEnabled(false);
        } else if (gameMode == GameMode.GHOST) {
            drawingView.mGoThroughTunnelEnabled = true;
        }

        new CountDownTimer(3000, 100) {
            @Override
            public void onTick(long l) {
                int seconds = (int) (l / 1000);
                int milliSeconds = (int) (l % 1000) / 100;
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
                if (gameMode == GameMode.PACMAN) {
                    drawingView.goInvisible(DEFAULT_PACMAN_VISIBILTY);
                } else if (gameMode == GameMode.GHOST) {
                    drawingView.mGoThroughTunnelEnabled = false;
                }
            }
        }, 3000);


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                invisibleButton.setEnabled(true);
            }
        }, 12000);

    }

    @Override
    public void setPercentage(final String percentage) {
        //percentageTV
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                percentageTV.setText(percentage + "%");
            }
        });
    }

    // Method to get touch events
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case (MotionEvent.ACTION_DOWN): {
                x1 = event.getX();
                y1 = event.getY();
            }
            break;
            case (MotionEvent.ACTION_UP): {
                x2 = event.getX();
                y2 = event.getY();
                drawingView.processTouchEvent(x1, y1, x2, y2);
            }
            break;
        }
        return true;
    }

    private void initJoyStick() {
        JoystickView joystick = findViewById(R.id.joystick);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                // Angles -
                // Up 45-135 , Left 135-225 , Down 225-315 , Right 315-45
                Log.d(TAG, "onMove: angle = " + angle);
                if (angle >= 45 && angle < 135) {
                    Log.d(TAG, "onMove: Up");
                    drawingView.setNextDirection(0);
                } else if (angle >= 135 && angle < 225) {
                    Log.d(TAG, "onMove: Left");
                    drawingView.setNextDirection(3);
                } else if (angle >= 225 && angle < 315) {
                    Log.d(TAG, "onMove: Down");
                    drawingView.setNextDirection(2);
                } else if ((angle >= 315 && angle < 360) || (angle > 0 && angle < 45)){
                    Log.d(TAG, "onMove: Right");
                    drawingView.setNextDirection(1);
                }
            }
        },500);
    }

    @Override
    public void onBackPressed() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.exit_match_title)
                    .setMessage(R.string.exit_match_message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PlayActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);
            AlertDialog dialog = builder.create();
            dialog.show();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.goInvisible): {
                goInvisible();
            } break;
        }
    }
}
