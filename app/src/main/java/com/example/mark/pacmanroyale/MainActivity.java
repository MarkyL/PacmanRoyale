package com.example.mark.pacmanroyale;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.mark.pacmanroyale.Fragments.PlayGround;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 0;
    private TextView mTextMessage;
    private FirebaseAuth auth;

    private static MediaPlayer player;

    private android.support.v4.app.FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    PlayGround playgroundFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    return true;
                case R.id.navigation_dashboard:


//                    fragmentManager = getSupportFragmentManager();
//                    fragmentTransaction = fragmentManager.beginTransaction();
//                    playgroundFragment = new PlayGround();
//                    Bundle bundleForPlayGround = new Bundle();
//                    bundleForPlayGround.putInt("map size",100);
//                    playgroundFragment.setArguments(bundleForPlayGround);
//                    fragmentManager.beginTransaction()
//                            .add(R.id.mapFrame, playgroundFragment, "playGroundFragment")
//                            .commit();


                    return true;
                case R.id.navigation_notifications:

                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        player = MediaPlayer.create(this, R.raw.pacman_song);
//        player.setVolume(100, 100);
//        player.setLooping(true);
//        player.start();

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // user already signed in
            Log.d(TAG, auth.getCurrentUser().getEmail());
        } else {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                            new AuthUI.IdpConfig.FacebookBuilder().build()))
                    .build(), 1);
        }
        findViewById(R.id.logoutBtn).setOnClickListener(this);
    }

    // Method to start activity for Play button
    public void showPlayScreen(View view) {
        Intent playIntent = new Intent(this, PlayActivity.class);
        startActivity(playIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // user logged in 
                Log.d(TAG, auth.getCurrentUser().getEmail());
            }
        } else {
            // User not authenticated
            Log.d(TAG, "Not Authenticated");
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.logoutBtn){
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(TAG, "User Logged out");
                    finish();
                }
            });
        }
    }

    public static MediaPlayer getPlayer() {
        return player;
    }
}
