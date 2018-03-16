package com.example.mark.pacmanroyale.Utilities;

import android.content.Context;

import com.example.mark.pacmanroyale.R;
import com.example.mark.pacmanroyale.User.UserInformation;
import com.example.mark.pacmanroyale.UserPresence;

/**
 * Created by Omri on 2/17/2018.
 */

public class UserInformationUtils {
    private static final String TAG = "UserInformationUtils";

    private static UserInformation userInformation;
    private static int enemyBlockSize;

    public static void setUserInformation(UserInformation information){
        userInformation = information;
    }

    public static UserInformation getUserInformation(){
        return userInformation;
    }

    public static void setUserPresenceOffline(Context context) {
        if (userInformation != null) {
            FireBaseUtils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.user_presence)).setValue(UserPresence.OFFLINE);
        }
    }

    public static void setUserPresenceSearchingForGhost(Context context) {
        if (userInformation != null) {
            FireBaseUtils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.user_presence)).setValue(UserPresence.SEARCHING_FOR_GHOST);
        }
    }

    public static void setUserPresenceSearchingForPacman(Context context) {
        if (userInformation != null) {
            FireBaseUtils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.user_presence)).setValue(UserPresence.SEARCHING_FOR_PACMAN);
        }
    }

    // TODO: implement quick match for phase 2.0
    public static void setUserPresenceSearchingForQuickMatch(Context context) {
        if (userInformation != null) {
            FireBaseUtils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.user_presence)).setValue(UserPresence.SEARCHING_FOR_QUICK_MATCH);
        }
    }

    public static void updateUsersPacmanWins(Context context) {
        if (userInformation != null) {
            FireBaseUtils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.pacman_node)).child(context.getString(R.string.wins)).setValue(userInformation.getPacman().getWins());
        }
    }

    public static void updateUsersPacmanTotalGames(Context context) {
        if (userInformation != null) {
            FireBaseUtils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.pacman_node)).child(context.getString(R.string.totalGames)).setValue(userInformation.getPacman().getTotalGames());
        }
    }

    public static void updateUsersGhostWins(Context context) {
        if (userInformation != null) {
            FireBaseUtils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.ghost_node)).child(context.getString(R.string.wins)).setValue(userInformation.getGhost().getWins());
        }
    }

    public static void updateUsersGhostWinRatio(Context context) {
        if (userInformation != null) {
            FireBaseUtils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.ghost_node)).child(context.getString(R.string.winRatio)).setValue(userInformation.getGhost().getWinRatio());
        }
    }

    public static void updateUsersPacmanWinRatio(Context context) {
        if (userInformation != null) {
            FireBaseUtils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.pacman_node)).child(context.getString(R.string.winRatio)).setValue(userInformation.getPacman().getWinRatio());
        }
    }

    public static void updateUsersGhostTotalGames(Context context) {
        if (userInformation != null) {
            FireBaseUtils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.ghost_node)).child(context.getString(R.string.totalGames)).setValue(userInformation.getGhost().getTotalGames());
        }
    }

    public static void setUserPresencePlaying(Context context) {
        if (userInformation != null) {
            FireBaseUtils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.user_presence)).setValue(UserPresence.PLAYING);
        }
    }

    public static void setUserPresenceOnline(Context context) {
        if (userInformation != null) {
            FireBaseUtils.getUserFireBaseDataBaseReference(context).child(context.getString(R.string.user_presence)).setValue(UserPresence.ONLINE);
        }
    }

    public static int getEnemyBlockSize() {
        return enemyBlockSize;
    }

    public static void setEnemyBlockSize(int enemyBlock) {
        enemyBlockSize = enemyBlock;
    }

}
