package com.example.mark.pacmanroyale.Tabs;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.mark.pacmanroyale.Activities.PlayActivity;
import com.example.mark.pacmanroyale.Enums.GameMode;
import com.example.mark.pacmanroyale.R;
import com.example.mark.pacmanroyale.Utilities.FireBaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Omri on 3/10/2018.
 */

public class Tab_Pacman_Skills extends Fragment {

    private static final String TAG = "Tab_Pacman_Skills";

    TextView pacmanNumWins;
    TextView pacmanNumGames;
    TextView pacmanWinRatio;
    Button invisibleSkillButton;
    private int wins = -1;
    private int totalGames = -1;
    private String winRatio =null;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_pacman_skills,container,false);
        pacmanNumWins = rootView.findViewById(R.id.pacmanNumWins);
        pacmanNumGames = rootView.findViewById(R.id.pacmanNumGames);
        pacmanWinRatio = rootView.findViewById(R.id.pacmanWinRatio);
        invisibleSkillButton = rootView.findViewById(R.id.pacmanSkillButton);
        invisibleSkillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSkill();
            }
        });

        //updateStatViews();

        return rootView;
    }

    private void showSkill() {

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
                View mView = getLayoutInflater().inflate(R.layout.skill_dialog, null);
                TextView description = mView.findViewById(R.id.description);
                Button endGameButton = mView.findViewById(R.id.skillButton);

                String skillDescription = "Shh... you are INVISIBLE!\nGhosts can't see you but they can sense your presence..\nWATCH YOUR BACK!";
                //String loseMsg = "Ohh... have a better luck next time";
                description.setText(skillDescription);


                mBuilder.setView(mView);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
            }


    @Override
    public void onResume() {
        super.onResume();
        updateStatViews();
    }

    private void updateStatViews() {

        FireBaseUtils.getFireBasePacmanNodeReference(getContext()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(getResources().getString(R.string.wins))) {
                    int wins = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.wins)).getValue().toString());
                    pacmanNumWins.setText(""+wins);
                }


                if (dataSnapshot.hasChild(getResources().getString(R.string.totalGames))) {
                    int totalGames = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.totalGames)).getValue().toString());
                    pacmanNumGames.setText(""+totalGames);
                }

                if (dataSnapshot.hasChild(getResources().getString(R.string.winRatio))) {
                   String winRatio = dataSnapshot.child(getResources().getString(R.string.winRatio)).getValue().toString();
                    pacmanWinRatio.setText(""+winRatio);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }









}
