package com.example.mark.pacmanroyale.GameLogic;

import com.example.mark.pacmanroyale.MiscDesign.CellInfo;

/**
 * Created by Mark on 13/02/2018.
 */

public class GameMap {

    private CellInfo[][] mMyPlayGround;

    public GameMap(int size) {
        this.mMyPlayGround = new CellInfo[size][size];
        initMyPlayGround();
    }

    private void initMyPlayGround() {
        for (int i=0;i<mMyPlayGround.length;i++){
            for (int j=0;j<mMyPlayGround.length;j++){
                mMyPlayGround[i][j]=null;//new CellInfo(null,null);
            }
        }
    }
}
