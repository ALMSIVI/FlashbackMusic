package cse110.team19.flashbackmusic;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper class for Location objects
 * Each location is associated with a list of tracks played at that location
 * Contains a list of strings which are the URLs to the download links of each track
 * Makes it easier to download Vibe Mode playlist
 * Created by Tyler on 3/13/18.
 */

public class LocationWrapper {
    private String location;

    // list of URLs to download links of each track played at location
    private List<String> tracks;

    public LocationWrapper() {
        tracks = new ArrayList<String>();
    }

    public LocationWrapper(String location) {
        this.location = location;
        tracks = new ArrayList<String>();
    }

    // getters and setters
    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getTracks() {
        return tracks;
    }
}
