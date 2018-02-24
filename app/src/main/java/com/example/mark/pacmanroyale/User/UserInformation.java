package com.example.mark.pacmanroyale.User;

import com.example.mark.pacmanroyale.VirtualGameRoom;

/**
 * Created by Omri on 2/17/2018.
 */

public class UserInformation {

    private String userId;
    private Pacman pacman;
    private Ghost ghost;

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
