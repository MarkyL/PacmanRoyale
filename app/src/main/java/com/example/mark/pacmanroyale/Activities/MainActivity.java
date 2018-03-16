package com.example.mark.pacmanroyale.Activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
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
import com.example.mark.pacmanroyale.Utilities.FireBaseUtils;
import com.example.mark.pacmanroyale.Utilities.UserInformationUtils;
import com.example.mark.pacmanroyale.Utilities.WaitingRoomUtils;
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
    private static final int PLAY_TAB = 1;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private UserInformation userInformation;

    private Tab_Skills tab_skills;
    private Tab_Play tab_play;
    private Tab_Settings tab_settings;

    private ViewPager mViewPager;
    private ImageView loadingScreen;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout tabLayout;


    /**
     * The {@link ViewPager} that will host the section contents.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingScreen = findViewById(R.id.loading_screen);
        tabLayout = findViewById(R.id.tabs);



        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingScreen.setVisibility(View.GONE);
                tabLayout.setVisibility(View.VISIBLE);
                mViewPager.setVisibility(View.VISIBLE);

            }
        }, 4000);

        printHashKey();

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //TabLayout tabLayout = findViewById(R.id.tabs);


        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        tabLayout.getTabAt(PLAY_TAB).select();

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

    // Used for sending x/y positions according to enemy's blockSize.
    // I'm setting my screen width, he will listen to this path and retrieve it
    // to convert his x/y to my x/y coordinates.
    private void setScreenWidthSize() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        FireBaseUtils.getFireBaseDataBase().child(getString(R.string.users_node)).child(mFirebaseAuth.getUid()).child(getString(R.string.screenWidth)).setValue(screenWidth);
    }

    // set user status to ONLINE, turn OFFLINE when he goes off.
    private void setUserPresence() {
        FireBaseUtils.getUserFireBaseDataBaseReference(this).child(getString(R.string.user_presence)).setValue(UserPresence.ONLINE);
        FireBaseUtils.getUserFireBaseDataBaseReference(this).child(getString(R.string.user_presence)).onDisconnect().setValue(UserPresence.OFFLINE);
    }


    // This function loads the user's details such as - ghost/pacman level and exp.
    public void loadUserDetails() {
        final String currentUserId = mFirebaseAuth.getUid();
        userInformation = new UserInformation();
        userInformation.setUserId(currentUserId);
        UserInformationUtils.setUserInformation(userInformation);
        FireBaseUtils.getFireBaseDataBase().child(getResources().getString(R.string.users_node)).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "loadUserDetails() - onDataChange ");
                userInformation = dataSnapshot.getValue(UserInformation.class);
                userInformation.setUserId(dataSnapshot.getKey());
                setUserPresence();
                UserInformationUtils.setUserInformation(userInformation);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "loadUserDetails() - onCancelled: " + databaseError.getMessage());
            }
        });
    }

    // initializing the waiting room as soon as I'm in the app,
    // to load all the current waiting player lists.
    public void initWaitingRoom() {
        final WaitingRoom waitingRoom = new WaitingRoom(this);
        WaitingRoomUtils.setWaitingRoom(waitingRoom);

        FireBaseUtils.getFireBaseDataBase().child(getString(R.string.waiting_room)).addValueEventListener(new ValueEventListener() {
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
                    WaitingRoomUtils.getWaitingRoom().setPacmanWaitingList(pacmanWaitingList);
                }
                if (ghostWaitingMap != null) {
                    ghostWaitingList = new ArrayList<>(ghostWaitingMap.keySet());
                    WaitingRoomUtils.getWaitingRoom().setGhostWaitingList(ghostWaitingList);
                }
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
        UserInformationUtils.setUserPresenceOffline(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserInformationUtils.setUserPresenceOnline(this);
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


        public void printHashKey() {
        // used to generate a hash key for SHA-1 code.
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
