package com.example.mark.pacmanroyale.MiscDesign;

/**
 * Created by Mark on 13/02/2018.
 */

public class CellInfo {
    private int mCellIndicator;
    private int imgResourceID;

    public CellInfo(int indicator, int imgID) {
        this.mCellIndicator = indicator;
        this.imgResourceID = imgID;
    }

    public int getmCellIndicator() {
        return mCellIndicator;
    }

    public void setmCellIndicator(int mCellIndicator) {
        this.mCellIndicator = mCellIndicator;
    }

    public int getImgResourceID() {
        return imgResourceID;
    }

    public void setImgResourceID(int imgResourceID) {
        this.imgResourceID = imgResourceID;
    }
}
