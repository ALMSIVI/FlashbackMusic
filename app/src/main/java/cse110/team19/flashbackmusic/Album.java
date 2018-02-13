package cse110.team19.flashbackmusic;

import java.util.ArrayList;

/**
 * Created by Meeta on 2/12/18.
 */

public class Album {
    String title;
    String artist;
    ArrayList<Track> tracks = new ArrayList();

    public Album (String t, String a)
    {
        title = t;
        artist = a;
    }

}
