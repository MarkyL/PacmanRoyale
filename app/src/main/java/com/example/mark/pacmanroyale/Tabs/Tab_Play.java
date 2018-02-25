package com.example.mark.pacmanroyale.Tabs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mark.pacmanroyale.Activities.PlayActivity;
import com.example.mark.pacmanroyale.Enums.GameMode;
import com.example.mark.pacmanroyale.R;
import com.example.mark.pacmanroyale.Utils;

/**
 * Created by Mark on 17/02/2018.
 */

public class Tab_Play extends Fragment implements View.OnClickListener {

    private static final String TAG = "Tab_Play";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_play, container, false);

        rootView.findViewById(R.id.playBtn).setOnClickListener(this);
        rootView.findViewById(R.id.playAsPacmanBtn).setOnClickListener(this);
        rootView.findViewById(R.id.playAsGhostBtn).setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case (R.id.playBtn): {
                Utils.setUserPresenceSearchingForGhost(getContext());
                Intent playIntent = new Intent(getContext(), PlayActivity.class);
                startActivity(playIntent);
            }
            break;
            case (R.id.playAsPacmanBtn): {
                Utils.getWaitingRoom().beginMatchMaking(GameMode.PACMAN);
            }
            break;
            case (R.id.playAsGhostBtn): {
                Utils.getWaitingRoom().beginMatchMaking(GameMode.GHOST);
            }
            break;
        }
    }

    }

