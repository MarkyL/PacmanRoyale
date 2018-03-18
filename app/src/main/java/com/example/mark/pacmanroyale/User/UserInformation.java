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
    private boolean joystick;
    private boolean music;
    private boolean sfx;

    private String status;
    private VirtualGameRoom virtualGameRoom;
    
    public UserInformation(){   }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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
    
    public int getScreenwidth() {
        return screenwidth;
    }

    public boolean isJoystick() {
        return joystick;
    }

    public void setJoystick(boolean joystick) {
        this.joystick = joystick;
    }

    public boolean isMusic() {
        return music;
    }

    public void setMusic(boolean music) {
        this.music = music;
    }

    public boolean isSfx() {
        return sfx;
    }

    public void setSfx(boolean sfx) {
        this.sfx = sfx;
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
