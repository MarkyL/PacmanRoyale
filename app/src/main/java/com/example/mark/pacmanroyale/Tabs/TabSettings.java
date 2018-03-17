package com.example.mark.pacmanroyale.Tabs;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.example.mark.pacmanroyale.R;
import com.example.mark.pacmanroyale.Utilities.FireBaseUtils;
import com.example.mark.pacmanroyale.Utilities.UserInformationUtils;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Mark on 17/02/2018.
 */

public class TabSettings extends Fragment implements View.OnClickListener{
    private static final String TAG = "TabSettings";

    private Button mLogoutBtn;
    private Button mCreditsBtn;
    private Switch mSFXSwitch;
    private Switch mMusicSwitch;
    private Switch mJoystickSwitch;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_settings, container, false);

        mLogoutBtn = rootView.findViewById(R.id.logOutBtn);
        mCreditsBtn = rootView.findViewById(R.id.credits);
        mSFXSwitch = rootView.findViewById(R.id.sfx_switch);
        mMusicSwitch = rootView.findViewById(R.id.music_switch);
        mJoystickSwitch = rootView.findViewById(R.id.joystick_switch);
        //set Switch State From Firebase
        setSwitchStateFromFireBase();

        //adding listeners to all clickables
        mLogoutBtn.setOnClickListener(this);
        mCreditsBtn.setOnClickListener(this);
        mSFXSwitch.setOnClickListener(this);
        mMusicSwitch.setOnClickListener(this);
        mJoystickSwitch.setOnClickListener(this);

        return rootView;
    }

    private void setSwitchStateFromFireBase() {

        FireBaseUtils.getUserFireBaseDataBaseReference(getContext()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(getResources().getString(R.string.joystick))){
                    boolean joystickBool = (boolean) dataSnapshot.child(getResources().getString(R.string.joystick)).getValue();
                    mJoystickSwitch.setChecked(joystickBool);
                }

                if(dataSnapshot.hasChild(getResources().getString(R.string.music))){
                    boolean musicBool = (boolean) dataSnapshot.child(getResources().getString(R.string.music)).getValue();
                    mMusicSwitch.setChecked(musicBool);
                }

                if(dataSnapshot.hasChild(getResources().getString(R.string.SFX))){
                    boolean sfxBool = (boolean) dataSnapshot.child(getResources().getString(R.string.SFX)).getValue();
                    mSFXSwitch.setChecked(sfxBool);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case (R.id.joystick_switch): {
               boolean isChecked =  mJoystickSwitch.isChecked();
               UserInformationUtils.getUserInformation().setJoystickEnabled(isChecked);
               FireBaseUtils.getUserFireBaseDataBaseReference(getContext()).child(getString(R.string.joystick)).setValue(isChecked);
            } break;
            case (R.id.music_switch): {
                boolean isChecked =  mMusicSwitch.isChecked();
                UserInformationUtils.getUserInformation().setJoystickEnabled(isChecked);
                FireBaseUtils.getUserFireBaseDataBaseReference(getContext()).child(getString(R.string.music)).setValue(isChecked);

            } break;
            case (R.id.sfx_switch): {
                boolean isChecked =  mSFXSwitch.isChecked();
                UserInformationUtils.getUserInformation().setJoystickEnabled(isChecked);
                FireBaseUtils.getUserFireBaseDataBaseReference(getContext()).child(getString(R.string.SFX)).setValue(isChecked);

            } break;
            case (R.id.credits): {

                showCredits();

            } break;
            case (R.id.logOutBtn): {
               // FirebaseAuth.getInstance().signOut();
                UserInformationUtils.setUserPresenceOffline(getContext());
                AuthUI.getInstance()
                        .signOut(getContext())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d(TAG, "onClick: log out");
                                getActivity().finish();
                            }
                        });

            } break;
        }
    }

    private void showCredits() {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
        View mView = getLayoutInflater().inflate(R.layout.credits_dialog, null);
        TextView description = mView.findViewById(R.id.creditMsg);



        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }
}
