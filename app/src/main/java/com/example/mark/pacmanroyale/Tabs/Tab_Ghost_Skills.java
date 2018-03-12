package com.example.mark.pacmanroyale.Tabs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mark.pacmanroyale.R;

/**
 * Created by Omri on 3/10/2018.
 */

public class Tab_Ghost_Skills extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_ghost_skills,container,false);
        return rootView;
    }
}
