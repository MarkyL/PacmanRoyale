package com.example.mark.pacmanroyale;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.example.mark.pacmanroyale.Activities.PlayActivity;
import com.example.mark.pacmanroyale.Enums.GameMode;
import com.example.mark.pacmanroyale.Utilities.FireBaseUtils;
import com.example.mark.pacmanroyale.Utilities.UserInformationUtils;
import com.example.mark.pacmanroyale.Utilities.VirtualRoomUtils;
import com.example.mark.pacmanroyale.Utilities.WaitingRoomUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.mark.pacmanroyale.DrawingView.BLOCK_SIZE_DIVIDER;


/**
 * Created by Mark on 21/02/2018.
 *
 * Waiting room contains infrastructure for future implementation of
 * queue line up of players, choosing the highest priority player
 * and management of the stacks.
 * therefore, there are methods/fields which are unused but are
 * necessary for this future implementations.
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
    private GameMode gameMode;

    private boolean foundMatch;
    private VirtualGameRoom gameRoom;
    private ValueEventListener inviteEventListener;

    private FindAMatchAsyncTask mAsyncTask;
    DatabaseReference dbWaitingListReference;
    private boolean mIsMatchMakingRelevantForGhost = false;
    private boolean mIsMatchMakingRelevantForPacman = false;

    /* TODO: Future implementation */
    private int myPositionInWaitingList;
    private boolean amInvited;
    private Thread mThread;

    public WaitingRoom(Context context) {
        this.context = context;
        this.userID = UserInformationUtils.getUserInformation().getUserId();
    }

    public void setPacmanWaitingList(ArrayList<String> pacmanWaitingList) {
        this.pacmanWaitingList = pacmanWaitingList;
    }

    public void setGhostWaitingList(ArrayList<String> ghostWaitingList) {
        this.ghostWaitingList = ghostWaitingList;
    }

    public void beginMatchMaking(GameMode playMode) {
        amInvited = false;
        initArrayListsIfNeeded();
        switch (playMode) {
            case PACMAN: {  
                // I want to team up with a ghost - INVITE it + create a private virtual room path in db.
                gameMode = GameMode.PACMAN;
                foundMatch = false;
                UserInformationUtils.setUserPresenceSearchingForGhost(context);
                dbWaitingListReference = FireBaseUtils.getFireBasePacmanWaitingList(context).child(userID);
                dbWaitingListReference.setValue(NULL);
                dbWaitingListReference.onDisconnect().removeValue();
                mAsyncTask = new FindAMatchAsyncTask(dbWaitingListReference);
                mAsyncTask.execute();
            }
            break;
            case GHOST: {
                gameMode = GameMode.GHOST;
                UserInformationUtils.setUserPresenceSearchingForPacman(context);
                dbWaitingListReference = FireBaseUtils.getFireBaseGhostWaitingList(context).child(userID);
                dbWaitingListReference.setValue(NULL);
                dbWaitingListReference.onDisconnect().removeValue();
                listenToInvites(dbWaitingListReference);
            }
            break;
            case QUICK_MATCH: { // FUTURE IMPLEMENTATIONS

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
        mIsMatchMakingRelevantForGhost = true;
        inviteEventListener = dbWaitingListReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange() called. dataSnapshot = " + dataSnapshot.getValue());
                if (dataSnapshot.getValue() == null) { // I think it means this node was remove - I found match myself.
                    dbWaitingListReference.removeEventListener(inviteEventListener);
                    dbWaitingListReference.removeValue();
                    return;
                }
                if (dataSnapshot.getValue().equals(NULL) || !mIsMatchMakingRelevantForGhost) {
                    return;
                }
                Log.d(TAG, "onDataChange() - I was invited to play by " + dataSnapshot.getValue());
                // should now implement - open a new virtual room with My ID + the dataSnapshop.getValue() ID.
                amInvited = true;
                enemyID = dataSnapshot.getValue().toString();
                retrieveEnemyBlockSize();

                gameRoom = new VirtualGameRoom(userID, enemyID, false);
                VirtualRoomUtils.setVirtualGameRoom(gameRoom);

                String virtualRoomID = enemyID + "+" + userID;
                Log.d(TAG, "ghost sets his virtualRoomID params to = " + virtualRoomID);
                DatabaseReference virtualRoomReference = FireBaseUtils.getFireBaseVirtualRoomReference(context).child(virtualRoomID);
                virtualRoomReference.child(context.getString(R.string.ghost_id)).setValue(userID);
                virtualRoomReference.child(context.getString(R.string.ghost_node)).child(context.getString(R.string.level)).setValue(UserInformationUtils.getUserInformation().getPacman().getLevel());
                virtualRoomReference.child(context.getString(R.string.ghost_node)).child(context.getString(R.string.experience)).setValue(UserInformationUtils.getUserInformation().getPacman().getExperience());
                virtualRoomReference.onDisconnect().removeValue();
                VirtualRoomUtils.setVirtualRoomReference(virtualRoomReference);

                dbWaitingListReference.removeValue();

                UserInformationUtils.setUserPresencePlaying(context);
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

    public void cancelPacmanGame() {
        cancelAsyncTask();
        dbWaitingListReference.removeValue();
        UserInformationUtils.setUserPresenceOnline(context);
        pacmanWaitingList.remove(userID);
    }

    private void cancelAsyncTask() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
            mIsMatchMakingRelevantForPacman = false;
            Log.d(TAG, "cancelAsyncTask: isCancelled = " + mAsyncTask.isCancelled());
        }
    }

    public void cancelGhostGame() {
        cancelListenToInvites();
        dbWaitingListReference.removeValue();
        UserInformationUtils.setUserPresenceOnline(context);
        ghostWaitingList.remove(userID);
        WaitingRoomUtils.getWaitingRoom().ghostWaitingList.remove(userID);
    }

    private void cancelListenToInvites() {
        if (dbWaitingListReference != null && inviteEventListener != null) {
            dbWaitingListReference.removeEventListener(inviteEventListener);
            mIsMatchMakingRelevantForGhost = false;
        }
    }

    private void retrieveEnemyBlockSize() {
            Log.d(TAG, "entered the if (enemyBlock size = 0)");
            FireBaseUtils.getFireBaseUsersNodeReference(context).child(enemyID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(context.getString(R.string.screenWidth))) {
                        int enemyScreenWidth = Integer.parseInt(dataSnapshot.child(context.getResources().getString(R.string.screenWidth)).getValue().toString());
                        int enemyBlockSize = enemyScreenWidth / BLOCK_SIZE_DIVIDER;
                        enemyBlockSize = (enemyBlockSize / 5) * 5;
                        UserInformationUtils.setEnemyBlockSize(enemyBlockSize);
                        Log.d(TAG, "retrieved enemyBlock size , now calling initEnemyPosVariables, enemy block size = "+enemyBlockSize);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
    }

    private class FindAMatchAsyncTask extends AsyncTask<String, Integer, String> {
        private static final String TAG = "FindAMatchAsyncTask";

        final DatabaseReference dbWaitingListReference;
        FindAMatchAsyncTask(final DatabaseReference dbWaitingListReference) {
            this.dbWaitingListReference = dbWaitingListReference;
        }

        @Override
        protected String doInBackground(String... strings) {
            mIsMatchMakingRelevantForPacman = true;
            while (!foundMatch && mIsMatchMakingRelevantForPacman) {
                if (ghostWaitingList.size() > 0) { // I found someone to play with
                    foundMatch = true;
                    enemyID = ghostWaitingList.get(0);
                    retrieveEnemyBlockSize();
                    FireBaseUtils.getFireBaseGhostWaitingList(context).child(enemyID).setValue(userID);
                    dbWaitingListReference.removeValue();
                    gameRoom = new VirtualGameRoom(userID, enemyID, true);
                    VirtualRoomUtils.setVirtualGameRoom(gameRoom);
                    //createGameRoomInFireBase();
                    Log.d(TAG, "doInBackground() finished - found a match!");
                }
            }
            dbWaitingListReference.removeValue();
            return "something";
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "onPostExecute() " + s);
            String virtualRoomID = userID + "+" + enemyID;
            Log.d(TAG, "onPostExecute() virtualRoomID = " + virtualRoomID);
            DatabaseReference virtualRoomReference = FireBaseUtils.getFireBaseVirtualRoomReference(context).child(virtualRoomID);
            virtualRoomReference.child(context.getString(R.string.pacman_id)).setValue(userID);
            virtualRoomReference.child(context.getString(R.string.pacman_node)).child(context.getString(R.string.level)).setValue(UserInformationUtils.getUserInformation().getPacman().getLevel());
            virtualRoomReference.child(context.getString(R.string.pacman_node)).child(context.getString(R.string.experience)).setValue(UserInformationUtils.getUserInformation().getPacman().getExperience());
            virtualRoomReference.onDisconnect().removeValue();
            VirtualRoomUtils.setVirtualRoomReference(virtualRoomReference);

            UserInformationUtils.setUserPresencePlaying(context);
            Intent playIntent = new Intent(context, PlayActivity.class);
            playIntent.putExtra(GAME_MODE, gameMode);
            context.startActivity(playIntent);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
           // Log.d(TAG, "onProgressUpdate() still looking for match");
        }
    }

    /* TODO: future implementation infrastructure. */
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

    public void setUpVirtualRoom() {
    }

    private void createGameRoomInFireBase() {
    }
}





