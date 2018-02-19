package cse110.team19.flashbackmusic;

import android.location.Location;

import java.util.*;

public class Track {
    private Calendar cal;
    private String trackName;
    private String artist;
    private int trackNumber;
    private int score;
    private int status;
    private int resourceId;
    private Location location;
    private long time;

    /**
     * Constructor.
     * @param trackName name of the song
     * @param trackNumber track number of the song
     * @param artist artist of the song
     */
    public Track(String trackName, int trackNumber, String artist, int resourceId) {
        this.trackName = trackName;
        this.trackNumber = trackNumber;
        this.artist = artist;
        this.resourceId = resourceId;
        score = 0;
        status = 0;
    }


    /**
     * -1: dislike
     * 0: neutral
     * 1: like
     */
    public void updateStatus() {
        if(status == -1) {
            status = 0;
        } else if(status == 0) {
            status = 1;
        } else if(status == 1) {
            status = -1;
        }
    }

    /**
     * Update the last played up the song.
     */
    public void justPlayed() {
        cal = Calendar.getInstance();

        if(NormalMode.recentlyPlayed.contains(this))
        {
            NormalMode.recentlyPlayed.remove(this);
        }
        NormalMode.recentlyPlayed.addFirst(this);
    }

    //Get the tracks time of day
    public String getTimePlayed() {
        if (cal == null) {
            return null;
        }
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if( 5 <= hour && hour < 11) {
            return "morning";
        } else if( 11 <= hour && hour < 17 ) {
            return "afternoon";
        } else {
            return "evening";
        }
    }

    //Get the tracks day of week
    public int getDayPlayed() {
        if (cal == null) {
            return 0;
        }
        int day = cal.get(Calendar.DAY_OF_WEEK);
        return day;
    }

    // Get info for flashback
    public String getTime() {
        if (cal == null) { // not implemented
            return null;
        } else {
            return cal.getTime().toString();
        }
    }

    public String getLocation() {
        if (location != null) {
            return location.getLatitude() + " " + location.getLongitude();
        } else {
            return "Unknown location";
        }
    }

    //Get time since last play
    public long getTimeSinceLastPlayed() {
        return time;
    }

    /* Getters and setters */
    public void setStatus(int status) {
        this.status = status;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getArtist() {
        return artist;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public int getScore() {
        return score;
    }

    public int getStatus() {
        return status;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setCalendar(Calendar calendar) {
        cal = calendar;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    //Increment to the score
    public void incrementScore(int toAdd)
    {
        this.score = this.score + toAdd;
    }

    //Make the score zero
    public void makeScoreNegative()
    {
        this.score = -1;
    }

    public void updateInfo(Location location, long time) {
        cal = Calendar.getInstance();
        this.location = location;
        this.time = time;
    }
}



