package cse110.team19.flashbackmusic;

import java.util.LinkedList;

/**
 * Created by YueWu on 3/12/2018.
 */

public class User {
    public String name;
    public String id;
    public LinkedList<Track> tracks;

    public User(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public Track getLatestTrack() {
        return tracks.getLast();
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public LinkedList<Track> getTracks() {
        return tracks;
    }


    //Setters
    public void setName(String name){this.name = name;}
    public void setId(String id) { this.id = id;}

    public void setTracks(LinkedList<Track> tracks) {
        this.tracks = tracks;
    }

}
