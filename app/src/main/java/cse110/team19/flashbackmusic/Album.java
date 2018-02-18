package cse110.team19.flashbackmusic;

import java.util.*;

/**
 * Created by Meeta on 2/12/18.
 */

public class Album {
    private String title;
    private String artist;
    private int numSongs;
    private Map<String, Track> tracks;

    public Album (String t, String a, int num) {
        title = t;
        artist = a;
        numSongs = num;
        tracks = new HashMap<String, Track>();
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

    public Track getTrack(String trackName) {
        return tracks.get(trackName);
    }

    public void addTrack(Track track) {
        tracks.put(track.getTrackName(), track);
    }
}
