package com.example.mark.pacmanroyale.Tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mark.pacmanroyale.R;

/**
 * Created by Mark on 17/02/2018.
 */

public class Tab_Play extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_play, container, false);

        return rootView;
    }

}
