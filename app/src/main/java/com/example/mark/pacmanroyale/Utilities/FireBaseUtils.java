package com.example.mark.pacmanroyale.Utilities;

import android.content.Context;

import com.example.mark.pacmanroyale.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Omri on 2/17/2018.
 */

public class FireBaseUtils {
    private static final String TAG = "FireBaseUtils";

    public static DatabaseReference getFireBaseDataBase(){
        return FirebaseDatabase.getInstance().getReference();
    }
    public static DatabaseReference getFireBaseUsersNodeReference(Context context) {
        return getFireBaseDataBase().child(context.getString(R.string.users_node));
    }

    public static DatabaseReference getUserFireBaseDataBaseReference(Context context){
        return FirebaseDatabase.getInstance().getReference(context.getString(R.string.users_node)).child(UserInformationUtils.getUserInformation().getUserId());
    }

    public static DatabaseReference getFireBasePacmanWaitingList(Context context) {
        return FirebaseDatabase.getInstance().getReference(context.getString(R.string.waiting_room)).child(context.getString(R.string.pacmanWaitingList));
    }

    public static DatabaseReference getFireBaseGhostWaitingList(Context context) {
        return FirebaseDatabase.getInstance().getReference(context.getString(R.string.waiting_room)).child(context.getString(R.string.ghostWaitingList));
    }

    public static DatabaseReference getFireBaseVirtualRoomReference(Context context) {
        return FirebaseDatabase.getInstance().getReference(context.getString(R.string.virtual_room));
    }

    public static DatabaseReference getFireBasePacmanNodeReference(Context context) {
        return FirebaseDatabase.getInstance().getReference(context.getString(R.string.users_node)).child(UserInformationUtils.getUserInformation().getUserId()).child(context.getString(R.string.pacman_node));
    }

    public static DatabaseReference getFireBaseGhostNodeReference(Context context) {
        return FirebaseDatabase.getInstance().getReference(context.getString(R.string.users_node)).child(UserInformationUtils.getUserInformation().getUserId()).child(context.getString(R.string.ghost_node));
    }
}
