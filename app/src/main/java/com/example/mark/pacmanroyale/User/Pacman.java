package com.example.mark.pacmanroyale.User;

/**
 * Created by Omri on 2/17/2018.
 */

public class Pacman {

    private int level;
    private int experience;
    private int xPos;
    private int yPos;

    public Pacman(){

    }

    public Pacman(int level, int experience, int xPos, int yPos) {
        this.level = level;
        this.experience = experience;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getxPos() {
        return xPos;
    }

    public void setxPos(int xPos) {
        this.xPos = xPos;
    }

    public int getyPos() {
        return yPos;
    }

    public void setyPos(int yPos) {
        this.yPos = yPos;
    }

    @Override
    public String toString() {
        return "Pacman{" +
                "level=" + level +
                ", experience=" + experience +
                ", xPos=" + xPos +
                ", yPos=" + yPos +
                '}';
    }
}
