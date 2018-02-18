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

    /**
     * Get the date and time this song was last played.
     */
    public String getDateTime() {
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);      // 0 to 11
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        String toRet = ("Last Played on: " + month + "/" + day + "/" + year + "at" +
                            hour + ":" + minute);
        return toRet;
    }

    //Get the tracks time of day
    public String getTimePlayed() {
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
    public int getDayPlayed()
    {
        int day = cal.get(Calendar.DAY_OF_WEEK);
        return day;
    }

    /* Getters and setters */
    public void setStatus(int status) {
        this.status = status;
    }

    public void setLocation(Location l) {
        location = l;
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

    //Get time since last play
    public long getTimeSinceLastPlayed() { return time; }
    //Set time since last play
    public void setTimeSinceLastPlayed(long time) { this.time = time; }

    public Calendar getCalendar() {
        return cal;
    }

    public int getResourceId() {
        return resourceId;
    }

    public Location getLocation() {
        return location;
    }


    //Increment to the score
    public void incrementScore(int toAdd)
    {
        this.score = this.score + toAdd;
    }

    //Make the score zero
    public void makeScoreZero()
    {
        this.score = 0;
    }

    public Set<String> getInfo() {
        LinkedHashSet<String> info = new LinkedHashSet<String>();
        info.add(Integer.toString(status));
        info.add(cal.toString());
        if (location != null) {
            info.add(location.toString());
        } else {
            info.add("Unknown location");
        }
        return info;
    }

    public void updateInfo() {
        cal = Calendar.getInstance();
    }
}



