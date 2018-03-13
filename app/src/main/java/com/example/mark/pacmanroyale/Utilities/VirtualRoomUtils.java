package com.example.mark.pacmanroyale.Utilities;

import com.example.mark.pacmanroyale.VirtualGameRoom;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by Omri on 2/17/2018.
 */

public class VirtualRoomUtils {
    private static final String TAG = "VirtualRoomUtils";

    private static DatabaseReference virtualRoomReference;

    private static VirtualGameRoom virtualGameRoom;

    public static void setVirtualRoomReference(DatabaseReference dbReference) {
        if (dbReference != null) {
            virtualRoomReference = dbReference;
        }
    }

    public static DatabaseReference getVirtualRoomReference() {
        return virtualRoomReference;
    }

    public static VirtualGameRoom getVirtualGameRoom() {
        return virtualGameRoom;
    }

    public static void setVirtualGameRoom(VirtualGameRoom virtualRoom) {
        virtualGameRoom = virtualRoom;
    }

}
