package com.example.mark.pacmanroyale.Tabs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mark.pacmanroyale.R;


/**
 * Created by Mark on 17/02/2018.
 */

public class Tab_Skills extends Fragment {
    private static final String TAG = "Tab_Skills";

    private ViewPager mViewPager;
    private Tab_Pacman_Skills tab_pacman_skills;
    private Tab_Play tab_play;

    private PagerAdapter mSectionsPagerAdapter;
    private Tab_Ghost_Skills tab_ghost_skills;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_skills, container, false);


        mSectionsPagerAdapter  = new PagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) rootView.findViewById(R.id.container_skills);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout)rootView.findViewById(R.id.tabs_skills);


        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        tab_pacman_skills = new Tab_Pacman_Skills();
        tab_ghost_skills = new Tab_Ghost_Skills();


        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        //initTextViewsListener();
    }

//    private void initTextViewsListener() {
//
//        FireBaseUtils.getUserFireBaseDataBaseReference(getContext()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                ghostLvl = getView().findViewById(R.id.ghostLvl);
//                pacmanLvl = getView().findViewById(R.id.pacmanLvl);
//                String userId = FireBaseUtils.getUserInformation().getUserId();
//                int pacmanLevel = -1,ghostLevel= -1;
//                for(DataSnapshot ds : dataSnapshot.getChildren()) {
//                    if (ds.getKey().equals(FireBaseUtils.getUserInformation().getUserId())) {
//                        pacmanLevel = ds.child(userId).getValue(UserInformation.class).getPacman().getLevel();
//                        ghostLevel = ds.child(userId).getValue(UserInformation.class).getGhost().getLevel();
//                    }
//                }
//                pacmanLvl.setText("pacman level: "+ pacmanLevel);
//                ghostLvl.setText("ghost level: " + ghostLevel);
//                Log.d(TAG, "onDataChange: ghost/pacman lvl has changed");
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

    public class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0: {
                    return tab_pacman_skills;
                }
                case 1: {
                    return tab_ghost_skills;
                }
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: {
                    return "PACMAN SKILLS";
                }
                case 1: {
                    return "GHOST SKILLS";
                }
                default:
                    return null;
            }
        }
    }


}


