package com.example.mark.pacmanroyale.Utilities;

import com.example.mark.pacmanroyale.WaitingRoom;

/**
 * Created by Omri on 2/17/2018.
 */

public class WaitingRoomUtils {
    private static final String TAG = "WaitingRoomUtils";

    private static WaitingRoom waitingRoom;

    public static WaitingRoom getWaitingRoom() {
        return waitingRoom;
    }

    public static void setWaitingRoom(WaitingRoom waitingRoom) {
        WaitingRoomUtils.waitingRoom = waitingRoom;
    }

}
