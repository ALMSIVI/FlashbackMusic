package cse110.team19.flashbackmusic;

import java.util.*;

/**
 * Created by Meeta on 2/12/18.
 */

public class Album {
    private String title;
    private String artist;
    private int numSongs;
    private List<Track> tracks;

    public Album (String t, String a, int num) {
        title = t;
        artist = a;
        numSongs = num;
        tracks = new ArrayList<Track>(numSongs);
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

    public Track getTrack(int i) {
        return tracks.get(i);
    }

    public void addTrack(Track track) {
        tracks.add(track);
    }
}
