package com.example.mark.pacmanroyale.Tabs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mark.pacmanroyale.R;
import com.example.mark.pacmanroyale.Utilities.FireBaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Omri on 3/10/2018.
 */

public class TabGhostSkills extends Fragment {

    TextView ghostNumWins;
    TextView ghostNumGames;
    TextView ghostWinRatio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_ghost_skills,container,false);
        ghostNumWins = rootView.findViewById(R.id.ghostNumWins);
        ghostNumGames = rootView.findViewById(R.id.ghostNumGames);
        ghostWinRatio = rootView.findViewById(R.id.ghostWinRatio);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatViews();
    }

    private void updateStatViews() {

        FireBaseUtils.getFireBaseGhostNodeReference(getContext()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(getResources().getString(R.string.wins))) {
                    int wins = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.wins)).getValue().toString());
                    ghostNumWins.setText(""+wins);
                }


                if (dataSnapshot.hasChild(getResources().getString(R.string.totalGames))) {
                    int totalGames = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.totalGames)).getValue().toString());
                    ghostNumGames.setText(""+totalGames);
                }

                if (dataSnapshot.hasChild(getResources().getString(R.string.winRatio))) {
                    String winRatio = dataSnapshot.child(getResources().getString(R.string.winRatio)).getValue().toString();
                    ghostWinRatio.setText(""+winRatio);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
