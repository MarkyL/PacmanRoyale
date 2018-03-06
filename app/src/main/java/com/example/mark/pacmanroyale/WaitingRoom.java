package com.example.mark.pacmanroyale;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.mark.pacmanroyale.Activities.PlayActivity;
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
    private static final String GAME_MODE = "GAME_MODE";

    private ArrayList<String> pacmanWaitingList;
    private ArrayList<String> ghostWaitingList;
    private Context context;
    private String userID;
    private String enemyID;
    private boolean isPlayAsPacman; // true = player is pacman , false = player is ghost.
    private GameMode gameMode;

    private boolean foundMatch;
    private VirtualGameRoom gameRoom;
    private int myPositionInWaitingList;
    private boolean amInvited;
    private ValueEventListener inviteEventListener;
    private Thread mThread;


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

    public void beginMatchMaking(GameMode playMode) {
        amInvited = false;
        final DatabaseReference dbWaitingListReference;
        initArrayListsIfNeeded();
        switch (playMode) {
            case PACMAN: {  
                // I want to team up with a ghost - INVITE it + create a private virtual room path in db.
                gameMode = GameMode.PACMAN;
                //addPacmanPlayer(userID);
                foundMatch = false;
                Utils.setUserPresenceSearchingForGhost(context);
                dbWaitingListReference = Utils.getFireBasePacmanWaitingList(context).child(userID);
                dbWaitingListReference.setValue(NULL);
                dbWaitingListReference.onDisconnect().removeValue();
                //listenToInvites(dbWaitingListReference);
                // the following while loop should be written in a separate function - searchForMatch()
                FindAMatchAsyncTask asyncTask = new FindAMatchAsyncTask(dbWaitingListReference);
                asyncTask.execute("stam");

//                mThread = new Thread() {
//                    @Override
//                    public void run() {
//                        while (!foundMatch) {
//                            if (ghostWaitingList.size() > 0) { // I found someone to play with
//                                foundMatch = true;
//                                //dbWaitingListReference.removeEventListener(inviteEventListener);
//                                enemyID = ghostWaitingList.get(0);
//                               // ghostWaitingList.remove(0);
//                                //pacmanWaitingList.remove(myPositionInWaitingList);
//                                Utils.getFireBaseGhostWaitingList(context).child(enemyID).setValue(userID);
//                                dbWaitingListReference.removeValue();
//                                gameRoom = new VirtualGameRoom(userID, enemyID, true);
//                                createGameRoomInFireBase();
//                                Log.d(TAG, "run() before interrupt");
//                                this.interrupt();
//                                //dbWaitingListReference.removeEventListener(inviteEventListener);
//                            }
//                        }
//                        dbWaitingListReference.removeValue();
//                    }
//                    @Override
//                    public boolean isInterrupted() {
//                        return super.isInterrupted();
//                    }
//                };
//                mThread.start();

            }
            break;
            case GHOST: {
                gameMode = GameMode.GHOST;
                //addGhostPlayer(userID);
                Utils.setUserPresenceSearchingForPacman(context);
                dbWaitingListReference = Utils.getFireBaseGhostWaitingList(context).child(userID);
                dbWaitingListReference.setValue(NULL);
                dbWaitingListReference.onDisconnect().removeValue();
                listenToInvites(dbWaitingListReference);
                // the following while loop should be written in a separate function - searchForMatch()
//                mThread = new Thread() {
//                    @Override
//                    public void run() {
//                        boolean foundMatch = false;
//                        while (!foundMatch && !amInvited) {
//                            if (pacmanWaitingList.size() > 0) {
//                                foundMatch = true;
//                                enemyID = pacmanWaitingList.get(0);
//                                pacmanWaitingList.remove(0);
//                                ghostWaitingList.remove(myPositionInWaitingList);
//                                Utils.getFireBasePacmanWaitingList(context).child(enemyID).setValue(userID);
//                                dbWaitingListReference.removeValue();
//                                gameRoom = new VirtualGameRoom(userID, enemyID, false);
//                                //dbWaitingListReference.removeEventListener(inviteEventListener);
//                            }
//                        }
//                        dbWaitingListReference.removeValue();
//                    }
//                };
//                mThread.start();
            }
            break;
            case QUICK_MATCH: {

            }
            break;

        }
    }

    private void createGameRoomInFireBase() {
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
                Log.d(TAG, "onDataChange() called. dataSnapshop = " + dataSnapshot.getValue());
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
                retrieveEnemyBlockSize();
                // need also to remove myself from firebase.
//                if (gameMode == GameMode.PACMAN) {
//                    pacmanWaitingList.remove(myPositionInWaitingList);
//                    // need to remove the user from the waiting list.
//                    for (int i = 0; i < ghostWaitingList.size(); i++) {
//                        if (ghostWaitingList.get(i).equals(enemyID)) {
//                            ghostWaitingList.remove(i);
//                            break;
//                        }
//                    }
//                    gameRoom = new VirtualGameRoom(userID, enemyID, true);
//                    Toast.makeText(context, "INVITED =me as "+userID+" enemy as "+enemyID, Toast.LENGTH_SHORT).show();
//                }
//                else { // currently - let's say it's GameMode.GHOST
//                    ghostWaitingList.remove(myPositionInWaitingList);
//                    for (int i = 0; i < pacmanWaitingList.size(); i++) {
//                        if (pacmanWaitingList.get(i).equals(enemyID)) {
//                            pacmanWaitingList.remove(i);
//                            break;
//                        }
//                    }
                    gameRoom = new VirtualGameRoom(userID, enemyID, false);
                    Utils.setVirtualGameRoom(gameRoom);
                    Toast.makeText(context, "INVITED =me as "+userID+" enemy as "+enemyID, Toast.LENGTH_SHORT).show();

                String virtualRoomID = enemyID + "+" + userID;
                Log.d(TAG, "ghost sets his virtualRoomID params to = " + virtualRoomID);
                DatabaseReference virtualRoomReference = Utils.getFireBaseVirtualRoomReference(context).child(virtualRoomID);
                virtualRoomReference.child(context.getString(R.string.ghost_id)).setValue(userID);
                virtualRoomReference.child(context.getString(R.string.ghost_node)).child(context.getString(R.string.level)).setValue(Utils.getUserInformation().getPacman().getLevel());
                virtualRoomReference.child(context.getString(R.string.ghost_node)).child(context.getString(R.string.experience)).setValue(Utils.getUserInformation().getPacman().getExperience());
                virtualRoomReference.onDisconnect().removeValue();
                Utils.setVirtualRoomReference(virtualRoomReference);
                //  }
                dbWaitingListReference.removeValue();

                Utils.setUserPresencePlaying(context);
                Intent playIntent = new Intent(context, PlayActivity.class);
                playIntent.putExtra(GAME_MODE, gameMode);
                context.startActivity(playIntent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled()");
            }
        });
    }

    private void retrieveEnemyBlockSize() {

        //if (enemyBlockSize == 0) {
            Log.d(TAG, "entered the if (enemyblocksize = 0)");
            Utils.getFireBaseUsersNodeReference(context).child(enemyID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Log.d(TAG, "onDataChange: enemyblocksize="+enemyBlockSize);
                    if (dataSnapshot.hasChild(context.getString(R.string.screenWidth))) {
                        int enemyScreenWidth = Integer.parseInt(dataSnapshot.child(context.getResources().getString(R.string.screenWidth)).getValue().toString());
                        int enemyBlockSize = enemyScreenWidth / 17;//(int) (enemyScreenWidth * 0.04);////
                        enemyBlockSize = (enemyBlockSize / 5) * 5;
                        Utils.setEnemyBlockSize(enemyBlockSize);
                        Log.d(TAG, "retrieved enemyblocksize , now calling initEnemyPosVariables, enemy block size = "+enemyBlockSize);
                        //initEnemyPosVariables();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        //}
    }

    public void setUpVirtualRoom() {
        }


    private class FindAMatchAsyncTask extends AsyncTask<String, Integer, String> {
        private static final String TAG = "FindAMatchAsyncTask";

        final DatabaseReference dbWaitingListReference;
        public FindAMatchAsyncTask(final DatabaseReference dbWaitingListReference) {
            this.dbWaitingListReference = dbWaitingListReference;

        }

        @Override
        protected String doInBackground(String... strings) {
            while (!foundMatch) {
                if (ghostWaitingList.size() > 0) { // I found someone to play with
                    foundMatch = true;
                    //dbWaitingListReference.removeEventListener(inviteEventListener);
                    enemyID = ghostWaitingList.get(0);
                    retrieveEnemyBlockSize();
                    // ghostWaitingList.remove(0);
                    //pacmanWaitingList.remove(myPositionInWaitingList);
                    Utils.getFireBaseGhostWaitingList(context).child(enemyID).setValue(userID);
                    dbWaitingListReference.removeValue();
                    gameRoom = new VirtualGameRoom(userID, enemyID, true);
                    Utils.setVirtualGameRoom(gameRoom);
                    createGameRoomInFireBase();
                    Log.d(TAG, "doInBackground() finished - found a match!");
                    //dbWaitingListReference.removeEventListener(inviteEventListener);
                }
                //onProgressUpdate();
            }
            dbWaitingListReference.removeValue();
            return "something";
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "onPostExecute() " + s);
            String virtualRoomID = userID + "+" + enemyID;
            Log.d(TAG, "onPostExecute() virtualRoomID = " + virtualRoomID);
            DatabaseReference virtualRoomReference = Utils.getFireBaseVirtualRoomReference(context).child(virtualRoomID);
            virtualRoomReference.child(context.getString(R.string.pacman_id)).setValue(userID);
            virtualRoomReference.child(context.getString(R.string.pacman_node)).child(context.getString(R.string.level)).setValue(Utils.getUserInformation().getPacman().getLevel());
            virtualRoomReference.child(context.getString(R.string.pacman_node)).child(context.getString(R.string.experience)).setValue(Utils.getUserInformation().getPacman().getExperience());
            virtualRoomReference.onDisconnect().removeValue();
            Utils.setVirtualRoomReference(virtualRoomReference);

            Utils.setUserPresencePlaying(context);
            Intent playIntent = new Intent(context, PlayActivity.class);
            playIntent.putExtra(GAME_MODE, gameMode);
            context.startActivity(playIntent);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
           // Log.d(TAG, "onProgressUpdate() still looking for match");
        }
    }
}





