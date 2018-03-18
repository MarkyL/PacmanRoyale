package com.example.mark.pacmanroyale;

import android.util.Log;

import com.example.mark.pacmanroyale.User.Ghost;
import com.example.mark.pacmanroyale.User.Pacman;
import com.example.mark.pacmanroyale.Utilities.UserInformationUtils;

/**
 * Created by Mark on 24/02/2018.
 */

public class VirtualGameRoom {
    private static final String TAG = "VirtualGameRoom";

    private String userID1;
    Pacman myPacman;
    Ghost myGhost;

    private String userID2;
    Pacman enemyPacman;
    Ghost enemyGhost;

    //need to know who is pacman who is ghost.
    public VirtualGameRoom(String userID1, String userID2, boolean amIPacman) {
        this.userID1 = userID1;
        this.userID2 = userID2;

        if (amIPacman) { // I play as pacman
            myPacman = UserInformationUtils.getUserInformation().getPacman();
            //enemyGhost =
        }

        Log.d(TAG, "VirtualGameRoom() created, me = " + userID1 + " as " + (amIPacman ? "Pacman" : "Ghost")
                + "| enemy = " + userID2 + " as " + (amIPacman ? "Ghost" : "Pacman"));
    }
    public String getUserID2() {
        return userID2;
    }

}
