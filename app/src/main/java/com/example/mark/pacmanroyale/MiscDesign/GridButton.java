package com.example.mark.pacmanroyale.MiscDesign;

import android.content.Context;

import com.example.mark.pacmanroyale.R;

/**
 * Created by Mark on 22/11/2017.
 */
public class GridButton extends android.support.v7.widget.AppCompatButton {

    public enum State {EMPTY,POSSIBLE, INUSE};
    private int positionX;
    private int positionY;
    private State availability; //Enum of states.

    public GridButton(Context context) {
        super(context);
        availability= State.EMPTY;
        //isAvailable=true;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }
    public State checkAvailability() {
        return availability;
    }

    public void setAvailability(State s) {
        availability=s;
    }
    public void setDefaultDrawable(){
        this.setBackgroundResource(R.drawable.cell_border);
    }
}
