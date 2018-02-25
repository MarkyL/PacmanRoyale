package com.example.mark.pacmanroyale;

import android.content.Context;
import android.util.Log;

import com.example.mark.pacmanroyale.Enums.GameMode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * Created by Mark on 21/02/2018.
 */

public class WaitingRoom {
    private static final String TAG = "WaitingRoom";

    private static final String NULL = "NULL";

    private ArrayList<String> pacmanWaitingList;
    private ArrayList<String> ghostWaitingList;
    private Context context;
    private String userID;
    private String enemyID;
    private boolean isPlayAsPacman; // true = player is pacman , false = player is ghost.
    private GameMode gameMode;

    private VirtualGameRoom gameRoom;
    private int myPositionInWaitingList;
    private boolean amInvited;
    private ValueEventListener inviteEventListener;

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
        pacmanWaitingList.add(userID);
        myPositionInWaitingList = pacmanWaitingList.size() - 1;
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
        ghostWaitingList.add(userID);
        myPositionInWaitingList = ghostWaitingList.size() - 1;
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

    public void beginMatchMaking(GameMode gameMode) {
        boolean foundMatch = false;
        amInvited = false;
        DatabaseReference dbWaitingListReference;
        initArrayListsIfNeeded();
        switch (gameMode) {
            case PACMAN: {  // I want to team up with a ghost.
                gameMode = GameMode.PACMAN;
                addPacmanPlayer(userID);
                Utils.setUserPresenceSearchingForGhost(context);
                dbWaitingListReference = Utils.getFireBasePacmanWaitingList(context).child(userID);
                dbWaitingListReference.setValue(NULL);
                dbWaitingListReference.onDisconnect().removeValue();
                listenToInvites(dbWaitingListReference);
                // the following while loop should be written in a separate function - searchForMatch()
                while (!foundMatch && !amInvited) {
                    if (ghostWaitingList.size() > 0) { // I found someone to play with
                        foundMatch = true;
                        enemyID = ghostWaitingList.get(0);
                        ghostWaitingList.remove(0);
                        pacmanWaitingList.remove(myPositionInWaitingList);
                        Utils.getFireBaseGhostWaitingList(context).child(enemyID).setValue(userID);
                        dbWaitingListReference.removeValue();
                        gameRoom = new VirtualGameRoom(userID, enemyID, true);
                        dbWaitingListReference.removeEventListener(inviteEventListener);
                    }
                }
            }
            break;
            case GHOST: {
                gameMode = GameMode.GHOST;
                addGhostPlayer(userID);
                Utils.setUserPresenceSearchingForPacman(context);
                dbWaitingListReference = Utils.getFireBaseGhostWaitingList(context).child(userID);
                dbWaitingListReference.setValue(NULL);
                dbWaitingListReference.onDisconnect().removeValue();
                listenToInvites(dbWaitingListReference);
                // the following while loop should be written in a separate function - searchForMatch()
                while (!foundMatch && !amInvited) {
                    if (pacmanWaitingList.size() > 0) {
                        foundMatch = true;
                        //dbWaitingListReference.removeEventListener(inviteEventListener);
                        enemyID = pacmanWaitingList.get(0);
                        ghostWaitingList.remove(myPositionInWaitingList);
                        Utils.getFireBasePacmanWaitingList(context).child(enemyID).setValue(userID);
                        dbWaitingListReference.removeValue();
                        gameRoom = new VirtualGameRoom(userID, enemyID, false);
                    }
                }
            }
            break;
            case QUICK_MATCH: {

            }
            break;

        }
    }

    private void initArrayListsIfNeeded() {
        if (pacmanWaitingList == null) {
            pacmanWaitingList = new ArrayList<>();
        }
        if (ghostWaitingList == null) {
            ghostWaitingList = new ArrayList<>();
        }
    }

    private void listenToInvites(final DatabaseReference dbWaitingListReference) {
        inviteEventListener = dbWaitingListReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) { // I think it means this node was remove - I found match myself.
                    dbWaitingListReference.removeEventListener(inviteEventListener);
                    return;
                }
                if (dataSnapshot.getValue().equals(NULL)) {
                    return;
                }
                Log.d(TAG, "onDataChange() - I was invited to play by " + dataSnapshot.getValue());
                // should now implement - open a new virtual room with My ID + the dataSnapshop.getValue() ID.
                amInvited = true;
                enemyID = dataSnapshot.getValue().toString();
                if (gameMode == GameMode.PACMAN) {
                    pacmanWaitingList.remove(myPositionInWaitingList);
                    // need to remove the user from the waiting list.
                    for (int i = 0; i < ghostWaitingList.size(); i++) {
                        if (ghostWaitingList.get(i) == enemyID) {
                            ghostWaitingList.remove(i);
                            break;
                        }
                    }
                    gameRoom = new VirtualGameRoom(userID, enemyID, true);
                }
                else { // currently - let's say it's GameMode.GHOST
                    ghostWaitingList.remove(myPositionInWaitingList);
                    for (int i = 0; i < pacmanWaitingList.size(); i++) {
                        if (pacmanWaitingList.get(i) == enemyID) {
                            pacmanWaitingList.remove(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled()");
            }
        });
    }

    public void setUpVirtualRoom() {
        }
    }


