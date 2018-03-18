package com.example.mark.pacmanroyale.MiscDesign;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.mark.pacmanroyale.R;

/**
 * Created by Mark on 17/03/2018.
 */

public class MediaPlayerService extends Service {

    private static final String TAG = "MediaPlayerService";
    MediaPlayer backGroundPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate:");
        backGroundPlayer = MediaPlayer.create(this, R.raw.pacman_song);
        backGroundPlayer.setLooping(true); // infinite while supposed to play
        backGroundPlayer.setVolume(0.1f,0.1f);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:");
        backGroundPlayer.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        backGroundPlayer.stop();
        backGroundPlayer.release();
    }
}
