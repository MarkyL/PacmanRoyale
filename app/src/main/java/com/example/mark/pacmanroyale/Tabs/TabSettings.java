package com.example.mark.pacmanroyale.Tabs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.mark.pacmanroyale.R;
import com.example.mark.pacmanroyale.Utilities.UserInformationUtils;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by Mark on 17/02/2018.
 */

public class TabSettings extends Fragment implements View.OnClickListener{
    private static final String TAG = "TabSettings";

    private Button logoutBtn;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_settings, container, false);

        logoutBtn = rootView.findViewById(R.id.logOutBtn);
        logoutBtn.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
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
}
