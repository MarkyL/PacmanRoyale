package com.example.mark.pacmanroyale.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.mark.pacmanroyale.Enums.UserPresence;
import com.example.mark.pacmanroyale.MiscDesign.MediaPlayerService;
import com.example.mark.pacmanroyale.MiscDesign.SwipeableViewPager;
import com.example.mark.pacmanroyale.R;
import com.example.mark.pacmanroyale.Tabs.TabPlay;
import com.example.mark.pacmanroyale.Tabs.TabSettings;
import com.example.mark.pacmanroyale.Tabs.TabSkills;
import com.example.mark.pacmanroyale.User.UserInformation;
import com.example.mark.pacmanroyale.Utilities.FireBaseUtils;
import com.example.mark.pacmanroyale.Utilities.UserInformationUtils;
import com.example.mark.pacmanroyale.Utilities.VirtualRoomUtils;
import com.example.mark.pacmanroyale.Utilities.WaitingRoomUtils;
import com.example.mark.pacmanroyale.WaitingRoom;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements TabPlay.ISearchMatchInterface, TabSettings.IdestroyService {

    private static final String TAG = "MainActivity";
    private static final int PLAY_TAB = 1;

    private FirebaseAuth mFirebaseAuth;

    private UserInformation mUserInformation;

    private TabSkills mTabSkills;
    private TabPlay mTabPlay;
    private TabSettings mTabSettings;
    private TabLayout mTabLayout;

    private SwipeableViewPager mSwipeAbleViewPager;

    private boolean mShouldAllowBackPress = true;

    private ImageView mLoadingScreen;
    private Intent mBackgroundIntent;

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        showLoadingScreen();
        printHashKey(); // used to facebook hash keys...
        createSwipableActivity();

        mTabSkills = new TabSkills();
        mTabPlay = new TabPlay();
        mTabSettings = new TabSettings();

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email","public_profile"));

        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser != null) {
            // user already signed in
            Log.d(TAG, mFirebaseAuth.getCurrentUser().getEmail());
            setScreenWidthSize();
            loadUserDetails();
            initWaitingRoom();
        } else{ // user not logged in.
            loadLogInView();
        }
    }

    private void initUI() {
        mLoadingScreen = findViewById(R.id.loading_screen);
        mTabLayout = findViewById(R.id.tabs);
        mSwipeAbleViewPager = findViewById(R.id.container);
    }

    private void showLoadingScreen() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoadingScreen.setVisibility(View.GONE);
                toggleTabsVisibility(true);
                mSwipeAbleViewPager.setVisibility(View.VISIBLE);
            }
        }, 3000);
    }

    private void createSwipableActivity() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mSwipeAbleViewPager.setAdapter(mSectionsPagerAdapter);
        mSwipeAbleViewPager.mIsSwipingEnabled = true;
        mSwipeAbleViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mSwipeAbleViewPager));

        mTabLayout.getTabAt(PLAY_TAB).select();
    }

    // Used for sending x/y positions according to enemy's blockSize.
    // I'm setting my screen width, he will listen to this path and retrieve it
    // to convert his x/y to my x/y coordinates.
    private void setScreenWidthSize() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        if (mFirebaseAuth.getUid() != null) {
            FireBaseUtils.getFireBaseDataBase().child(getString(R.string.users_node))
                    .child(mFirebaseAuth.getUid()).child(getString(R.string.screenWidth))
                    .setValue(screenWidth);
        }
    }

    // set user status to ONLINE, turn OFFLINE when he goes off.
    private void setUserPresence() {
        FireBaseUtils.getUserFireBaseDataBaseReference(this).child(getString(R.string.user_presence)).setValue(UserPresence.ONLINE);
        FireBaseUtils.getUserFireBaseDataBaseReference(this).child(getString(R.string.user_presence)).onDisconnect().setValue(UserPresence.OFFLINE);
    }

    // This function loads the user's details such as - ghost/pacman level and exp.
    public void loadUserDetails() {
        final String currentUserId = mFirebaseAuth.getUid();
        mUserInformation = new UserInformation();
        mUserInformation.setUserId(currentUserId);
        UserInformationUtils.setUserInformation(mUserInformation);
        if (currentUserId != null) {
            FireBaseUtils.getFireBaseDataBase().child(getResources().getString(R.string.users_node)).child(currentUserId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "loadUserDetails() - onDataChange ");
                            mUserInformation = dataSnapshot.getValue(UserInformation.class);
                            mUserInformation.setUserId(dataSnapshot.getKey());
                            setUserPresence();
                            UserInformationUtils.setUserInformation(mUserInformation);
                            handleMediaPlayerService(UserInformationUtils.getUserInformation().isMusic());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "loadUserDetails() - onCancelled: " + databaseError.getMessage());
                        }
                    });
        }
    }

    // Initializing the waiting room as soon as I'm in the app,
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserInformationUtils.setUserPresenceOffline(this);
        if (VirtualRoomUtils.getVirtualRoomReference() != null) { // in case disconnect is late to work.
            VirtualRoomUtils.getVirtualRoomReference().removeValue();
        }
        if (mBackgroundIntent != null) {
            stopService(mBackgroundIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserInformationUtils.setUserPresenceOnline(this);
    }

    public void toggleTabsVisibility(boolean isTabsShown) {
        if (mTabLayout != null) {
            if (isTabsShown) {
                mTabLayout.setVisibility(View.VISIBLE);
            } else {
                mTabLayout.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void handleMediaPlayerService(boolean shouldStart) {
        if (shouldStart) {
            mBackgroundIntent = new Intent(this, MediaPlayerService.class);
            startService(mBackgroundIntent);
        } else { // i need to stop.
            if (mBackgroundIntent != null) {
                stopService(mBackgroundIntent);
            }
        }
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
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.tab_play, container, false);
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

    @Override
    public void onBackPressed() {
        if (mShouldAllowBackPress) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.exit_pacman_royale_title)
                    .setMessage(R.string.exit_pacman_royale_message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.super.onBackPressed();
                            UserInformationUtils.setUserPresenceOffline(MainActivity.this); // incase ondestroy won't catch it
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        // else do nothing... consume.
    }

    public void toggleBackPressAvailability(boolean isActive) {
        mShouldAllowBackPress = isActive;
    }

    @Override
    public void toggleSearchingForMatch(boolean isSearching) {
        if (isSearching) {
            toggleBackPressAvailability(false);
            toggleTabsVisibility(false);
            mSwipeAbleViewPager.mIsSwipingEnabled = false;
        } else {
            toggleBackPressAvailability(true);
            toggleTabsVisibility(true);
            mSwipeAbleViewPager.mIsSwipingEnabled = true;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0: {
                    return mTabSkills;
                }
                case 1: {
                    return mTabPlay;
                }
                case 2: {
                    return mTabSettings;
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
