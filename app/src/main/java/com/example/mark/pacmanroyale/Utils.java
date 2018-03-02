package com.example.mark.pacmanroyale;

import android.content.Context;

import com.example.mark.pacmanroyale.User.UserInformation;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Omri on 2/17/2018.
 */

public class Utils {
    private static final String TAG = "Utils";

    private static WaitingRoom waitingRoom;
    private static UserInformation userInformation;
    private static DatabaseReference virtualRoomReference;

    public static DatabaseReference getFireBaseDataBase(){
        return FirebaseDatabase.getInstance().getReference();
    }

    public static DatabaseReference getUserFireBaseDataBaseReference(Context context){
        return FirebaseDatabase.getInstance().getReference(context.getString(R.string.users_node)).child(userInformation.getUserId());
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

    public static void setVirtualRoomReference(DatabaseReference dbReference) {
        if (dbReference != null) {
            virtualRoomReference = dbReference;
        }
    }

    public static DatabaseReference getVirtualRoomReference() {
        return virtualRoomReference;
    }

    public static void setUserInformation(UserInformation information){
        userInformation = information;
    }

    public static WaitingRoom getWaitingRoom() {
        return waitingRoom;
    }

    public static void setWaitingRoom(WaitingRoom waitingRoom) {
        Utils.waitingRoom = waitingRoom;
    }

    public static UserInformation getUserInformation(){
        return userInformation;
    }

    public static void setUserPresenceOffline(Context context) {
        if (userInformation != null) {
            Utils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.user_presence)).setValue(UserPresence.OFFLINE);
        }
    }

    public static void setUserPresenceSearchingForGhost(Context context) {
        if (userInformation != null) {
            Utils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.user_presence)).setValue(UserPresence.SEARCHING_FOR_GHOST);
        }
    }

    public static void setUserPresenceSearchingForPacman(Context context) {
        if (userInformation != null) {
            Utils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.user_presence)).setValue(UserPresence.SEARCHING_FOR_PACMAN);
        }
    }

    public static void setUserPresenceSearchingForQuickMatch(Context context) {
        if (userInformation != null) {
            Utils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.user_presence)).setValue(UserPresence.SEARCHING_FOR_QUICK_MATCH);
        }
    }

    public static void setUserPresencePlaying(Context context) {
        if (userInformation != null) {
            Utils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.user_presence)).setValue(UserPresence.PLAYING);
        }
    }

    public static void setUserPresenceOnline(Context context) {
        if (userInformation != null) {
            Utils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.user_presence)).setValue(UserPresence.ONLINE);
        }
    }


}
