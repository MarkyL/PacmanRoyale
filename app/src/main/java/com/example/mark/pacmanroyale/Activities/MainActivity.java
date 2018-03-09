package com.example.mark.pacmanroyale.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mark.pacmanroyale.R;
import com.example.mark.pacmanroyale.Tabs.Tab_Play;
import com.example.mark.pacmanroyale.Tabs.Tab_Settings;
import com.example.mark.pacmanroyale.Tabs.Tab_Skills;
import com.example.mark.pacmanroyale.User.UserInformation;
import com.example.mark.pacmanroyale.UserPresence;
import com.example.mark.pacmanroyale.Utils;
import com.example.mark.pacmanroyale.WaitingRoom;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 0;
    private static final int BLOCK_SIZE_DIVIDER = 17;

    private TextView mTextMessage;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private UserInformation userInformation;


    private static MediaPlayer player;

    private android.support.v4.app.FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    Tab_Skills tab_skills;
    Tab_Play tab_play;
    Tab_Settings tab_settings;


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private ImageView loadingScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        loadingScreen = findViewById(R.id.loading_screen);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingScreen.setVisibility(View.GONE);
            }
        }, 3000);

        printHashKey(this);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        tab_skills = new Tab_Skills();
        tab_play = new Tab_Play();
        tab_settings = new Tab_Settings();

        boolean loggedIn = AccessToken.getCurrentAccessToken() == null;
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email","public_profile"));

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();


        if (mFirebaseUser != null) {
            // user already signed in
            Log.d(TAG, mFirebaseAuth.getCurrentUser().getEmail());
            setScreenWidthSize();
            loadUserDetails();
            initWaitingRoom();
        } else { // user not logged in.
            loadLogInView();
        }
    }

    private void setScreenWidthSize() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
//        int blockSize = screenWidth / BLOCK_SIZE_DIVIDER;
//        blockSize = (blockSize / 5) * 5;
        Utils.getFireBaseDataBase().child(getString(R.string.users_node)).child(mFirebaseAuth.getUid()).child(getString(R.string.screenWidth)).setValue(screenWidth);
    }

    private void setUserPresence() {
        Utils.getUserFireBaseDataBaseReference(this).child(getString(R.string.user_presence)).setValue(UserPresence.ONLINE);
        Utils.getUserFireBaseDataBaseReference(this).child(getString(R.string.user_presence)).onDisconnect().setValue(UserPresence.OFFLINE);
    }


    // This function loads the user's details such as - ghost/pacman level and exp.
    public void loadUserDetails() {
        final String currentUserId = mFirebaseAuth.getUid(); // .child(userId)
        userInformation = new UserInformation();
        userInformation.setUserId(currentUserId);
        Utils.setUserInformation(userInformation);
        Utils.getFireBaseDataBase().child(getResources().getString(R.string.users_node)).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userInformation = dataSnapshot.getValue(UserInformation.class);
                userInformation.setUserId(dataSnapshot.getKey());
//                    int pacmanLevel = ds.child(userInformation.getUserId()).getValue(UserInformation.class).getPacman().getLevel();
//                    int pacmanExperience = ds.child(userInformation.getUserId()).getValue(UserInformation.class).getPacman().getExperience();
//                    userInformation.setPacman(new Pacman(pacmanLevel, pacmanExperience, 0, 0));
//                    int ghostLevel = ds.child(userInformation.getUserId()).getValue(UserInformation.class).getGhost().getLevel();
//                    int ghostExperience = ds.child(userInformation.getUserId()).getValue(UserInformation.class).getGhost().getExperience();
//                    userInformation.setGhost(new Ghost(ghostLevel, ghostExperience, 0, 0));


                setUserPresence();
                Utils.setUserInformation(userInformation);
                //Log.d(TAG, "onDataChange:  pacman = " +userInformation.getPacman() );
                //Log.d(TAG, "onDataChange:  ghost = " +userInformation.getGhost());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initWaitingRoom() {
        final WaitingRoom waitingRoom = new WaitingRoom(this);
        Utils.setWaitingRoom(waitingRoom);

        Utils.getFireBaseDataBase().child(getString(R.string.waiting_room)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get waiting room changes to map
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                Map<String, ArrayList<String>> pacmanWaitingMap = (HashMap<String, ArrayList<String>>) dataSnapshot
                        .child(getString(R.string.pacmanWaitingList)).getValue();
                Map<String, ArrayList<String>> ghostWaitingMap = (HashMap<String, ArrayList<String>>) dataSnapshot
                        .child(getString(R.string.ghostWaitingList)).getValue();

                // pour the maps into array lists
                ArrayList<String> pacmanWaitingList;
                ArrayList<String> ghostWaitingList;
                if (pacmanWaitingMap != null) {
                    pacmanWaitingList = new ArrayList<>(pacmanWaitingMap.keySet());
                    Utils.getWaitingRoom().setPacmanWaitingList(pacmanWaitingList);
                }
                if (ghostWaitingMap != null) {
                    ghostWaitingList = new ArrayList<>(ghostWaitingMap.keySet());
                    Utils.getWaitingRoom().setGhostWaitingList(ghostWaitingList);
                }

                // update the waiting room lists

                //for (DataSnapshot ds : dataSnapshot.getChildren()) {
                //waitingRoom = ds.getValue(WaitingRoom.class);
                //Log.d(TAG, "onDataChange()");
                //pacmanWaitingList = (ArrayList<String>) ds.child(getString(R.string.pacmanWaitingList)).getValue();
                //map = (HashMap<String, ArrayList<String>>) dataSnapshot.getValue();

//                    List<String> values = (List<String>) td.values();
//                    waitingRoom.setPacmanWaitingList(new ArrayList<>(values));
                //}
                //Log.d(TAG, "onDataChange: map = "+td.toString());

                // Utils.setWaitingRoom(waitingRoom);
                //waitingRoom.setPacmanWaitingList(map.get(0));

                //}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled() error: " + databaseError.getMessage());
            }
        });
    }

    //TODO: remove this - since we clear activity stack there is no result!!!
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: recieving result");
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // user logged in
                Log.d(TAG,"user logged in : " + FirebaseAuth.getInstance().getCurrentUser().getEmail());
            }
        } else {
            // User not authenticated
            Log.d(TAG, "Not Authenticated");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.setUserPresenceOffline(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.setUserPresenceOnline(this);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.tab_play, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }




        public void printHashKey(Context pContext) {
            try {
                PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
                Log.d(TAG, "printHashKey: Im before loop" + info.toString());
                for (Signature signature : info.signatures) {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    String hashKey = new String(Base64.encode(md.digest(), 0));
                    Log.i(TAG, "printHashKey() Hash Key: " + hashKey);
                }
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "printHashKey()", e);
            } catch (Exception e) {
                Log.e(TAG, "printHashKey()", e);
            }
        }

    private void loadLogInView() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0: {
                    return tab_skills;
                }
                case 1: {
                    return tab_play;
                }
                case 2: {
                    return tab_settings;
                }
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: {
                    return "SKILLS";
                }
                case 1: {
                    return "PLAY";
                }
                case 2: {
                    return "SETTINGS";
                }
                default:
                    return null;
            }
        }


    }
}
