package com.example.mark.pacmanroyale;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Mark on 21/02/2018.
 */

public class WaitingRoom {
    private static final String TAG = "WaitingRoom";

    private ArrayList<String> pacmanWaitingList;
    private ArrayList<String> ghostWaitingList;
    private Context context;
    private String userID;
    private boolean isPlayAsPacman; // true = player is pacman , false = player is ghost.
    private GameMode gameMode;
    private enum GameMode {
        PACMAN,
        GHOST,
        QUICK_MATCH
    }
    public WaitingRoom(Context context) {
        this.context = context;
        this.userID = Utils.getUserInformation().getUserId();
    }
//
//    public WaitingRoom() {
//        this.pacmanWaitingList = new ArrayList<>();
//        this.ghostWaitingList = new ArrayList<>();
//    }

    public void addPacmanPlayer(String userID) {
        gameMode = GameMode.PACMAN;
        pacmanWaitingList.add(userID);
        Utils.setUserPresenceSearchingForGhost(context);
        Utils.getFireBasePacmanWaitingList(context).child(Utils.getUserInformation().getUserId()).setValue("searching");
        Utils.getFireBasePacmanWaitingList(context).child(Utils.getUserInformation().getUserId()).onDisconnect().removeValue();
        Utils.getFireBasePacmanWaitingList(context).child(Utils.getUserInformation().getUserId());
    }

    public String getPacmanPlayer() {
        if (pacmanWaitingList != null && pacmanWaitingList.size() > 0) {
            return pacmanWaitingList.remove(0);
        }
        return null;
    }

    public ArrayList<String> getPacmanWaitingList() {
        return pacmanWaitingList;
    }

    public void addGhostPlayer(String userID) {
        gameMode = GameMode.GHOST;
        ghostWaitingList.add(userID);
        Utils.setUserPresenceSearchingForPacman(context);
        Utils.getFireBaseGhostWaitingList(context).child(Utils.getUserInformation().getUserId()).setValue("searching for pacman");
        Utils.getFireBaseGhostWaitingList(context).child(Utils.getUserInformation().getUserId()).setValue("searching");
        Utils.getFireBaseGhostWaitingList(context).child(Utils.getUserInformation().getUserId()).onDisconnect().removeValue();
    }

    public String getGhostPlayer() {
        if (ghostWaitingList != null && ghostWaitingList.size() > 0) {
            return ghostWaitingList.remove(0);
        }
        return null;
    }

    public ArrayList<String> getGhostWaitingList() {
        return ghostWaitingList;
    }

    public void setPacmanWaitingList(ArrayList<String> pacmanWaitingList) {
        this.pacmanWaitingList = pacmanWaitingList;
    }

    public void setGhostWaitingList(ArrayList<String> ghostWaitingList) {
        this.ghostWaitingList = ghostWaitingList;
    }

    public void setUpVirtualRoom() {
        switch (gameMode) {
            case PACMAN: {

            } break;
            case GHOST: {

            } break;
            case QUICK_MATCH: {

            } break;
        }
    }
}
