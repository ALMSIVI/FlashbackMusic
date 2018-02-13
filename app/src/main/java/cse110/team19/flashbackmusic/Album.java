package cse110.team19.flashbackmusic;

import java.util.ArrayList;

/**
 * Created by Meeta on 2/12/18.
 */

public class Album {
    private String title;
    private String artist;
    private int numSongs;
    private ArrayList<Track> tracks = new ArrayList();

    public Album (String t, String a, int num) {
        title = t;
        artist = a;
        numSongs = num;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public int getNumSongs() {
        return numSongs;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }
}
