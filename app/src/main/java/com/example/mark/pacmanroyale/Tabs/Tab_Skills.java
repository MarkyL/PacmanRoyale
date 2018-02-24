package com.example.mark.pacmanroyale.Tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mark.pacmanroyale.R;
import com.example.mark.pacmanroyale.User.UserInformation;
import com.example.mark.pacmanroyale.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by Mark on 17/02/2018.
 */

public class Tab_Skills extends Fragment {
    private static final String TAG = "Tab_Skills";

    private TextView ghostLvl;
    private TextView pacmanLvl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_skills, container, false);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //initTextViewsListener();
    }

    private void initTextViewsListener() {

        Utils.getUserFireBaseDataBaseReference(getContext()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ghostLvl = getView().findViewById(R.id.ghostLvl);
                pacmanLvl = getView().findViewById(R.id.pacmanLvl);
                String userId = Utils.getUserInformation().getUserId();
                int pacmanLevel = -1,ghostLevel= -1;
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals(Utils.getUserInformation().getUserId())) {
                        pacmanLevel = ds.child(userId).getValue(UserInformation.class).getPacman().getLevel();
                        ghostLevel = ds.child(userId).getValue(UserInformation.class).getGhost().getLevel();
                    }
                }
                pacmanLvl.setText("pacman level: "+ pacmanLevel);
                ghostLvl.setText("ghost level: " + ghostLevel);
                Log.d(TAG, "onDataChange: ghost/pacman lvl has changed");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}


