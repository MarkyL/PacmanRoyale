package com.example.mark.pacmanroyale.User;

/**
 * Created by Omri on 2/17/2018.
 */

public class Ghost {

    private int level;
    private int experience;
    private int xPos;
    private int yPos;
    private int wins;
    private int totalGames;
    private String winRatio;

    public Ghost (){ }

    public Ghost(int level, int experience, int xPos, int yPos , int wins , int totalGames ) {
        this.level = level;
        this.experience = experience;
        this.xPos = xPos;
        this.yPos = yPos;
        this.wins = wins;
        this.totalGames = totalGames;
        this.winRatio = "0";
    }

    public String getWinRatio() {
        if(totalGames > 0) {
            float winRatio = (float)wins/totalGames;
            String winRatioStr = String.format("%.2f",winRatio);
            return winRatioStr;
        }
        return winRatio;
    }

    public void setWinRatio(String winRatio) {
        this.winRatio = winRatio;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int numOfWins) {
        this.wins = numOfWins;
    }

    public int getTotalGames() {
        return totalGames;
    }

    public void setTotalGames(int totalGames) {
        this.totalGames = totalGames;
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
        return "Ghost{" +
                "level=" + level +
                ", experience=" + experience +
                ", xPos=" + xPos +
                ", yPos=" + yPos +
                '}';
    }
}
