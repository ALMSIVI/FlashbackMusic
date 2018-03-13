package cse110.team19.flashbackmusic;

import java.util.LinkedList;

/**
 * Created by YueWu on 3/12/2018.
 */

public class User {
    public String name;
    public String email;
    public LinkedList<Track> tracks;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Track getLatestTrack() {
        return tracks.getLast();
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public LinkedList<Track> getTracks() {
        return tracks;
    }
}
