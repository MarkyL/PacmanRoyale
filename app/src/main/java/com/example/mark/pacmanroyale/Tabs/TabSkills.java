package com.example.mark.pacmanroyale.Tabs;

import android.os.Bundle;
import android.support.annotation.NonNull;
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

public class TabSkills extends Fragment {
    private static final String TAG = "TabSkills";

    private TabPacmanSkills mTabPacmanSkills;
    private TabGhostSkills mTabGhostSkills;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_skills, container, false);

        PagerAdapter mSectionsPagerAdapter = new PagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = rootView.findViewById(R.id.container_skills);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = rootView.findViewById(R.id.tabs_skills);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        mTabPacmanSkills = new TabPacmanSkills();
        mTabGhostSkills = new TabGhostSkills();
        return rootView;
    }

    public class PagerAdapter extends FragmentPagerAdapter {

        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0: {
                    return mTabPacmanSkills;
                }
                case 1: {
                    return mTabGhostSkills;
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


