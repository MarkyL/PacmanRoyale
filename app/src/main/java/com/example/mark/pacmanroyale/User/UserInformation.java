package com.example.mark.pacmanroyale.User;

import com.example.mark.pacmanroyale.VirtualGameRoom;

/**
 * Created by Omri on 2/17/2018.
 */

public class UserInformation {

    private String userId;
    private Pacman pacman;
    private Ghost ghost;
    //private int blocksize;
    private int screenwidth;

    // for settings
    private boolean isJoystickEnabled;
    private boolean isMusicEnabled;
    private boolean isSFXEnabled;

    private String status;
    private VirtualGameRoom virtualGameRoom;
    
    public UserInformation(){   }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
//    public UserPresence getUserPresence() {
//        return userPresence;
//    }
//
//    public void setUserPresence(UserPresence userPresence) {
//        this.userPresence = userPresence;
//    }

    public Pacman getPacman() {
        return pacman;
    }

    public void setPacman(Pacman pacman) {
        this.pacman = pacman;
    }

    public Ghost getGhost() {
        return ghost;
    }

    public void setGhost(Ghost ghost) {
        this.ghost = ghost;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

//    public int getblocksize() { return blocksize;  }
//
//    public void setblocksize(int blockSize) { this.blocksize = blockSize; }

    public int getScreenwidth() {
        return screenwidth;
    }

    public boolean isJoystickEnabled() {
        return isJoystickEnabled;
    }

    public void setJoystickEnabled(boolean joystickEnabled) {
        isJoystickEnabled = joystickEnabled;
    }

    public boolean isMusicEnabled() {
        return isMusicEnabled;
    }

    public void setMusicEnabled(boolean musicEnabled) {
        isMusicEnabled = musicEnabled;
    }

    public boolean isSFXEnabled() {
        return isSFXEnabled;
    }

    public void setSFXEnabled(boolean SFXEnabled) {
        isSFXEnabled = SFXEnabled;
    }

    public void setScreenwidth(int screenwidth) {
        this.screenwidth = screenwidth;
    }
    @Override
    public String toString() {
        return "UserInformation{" +
                "status='" + status + '\'' +
                ", Pacman=" + pacman +
                ", ghost=" + ghost +
                ", userId='" + userId + '\'' +
                '}';
    }
}
