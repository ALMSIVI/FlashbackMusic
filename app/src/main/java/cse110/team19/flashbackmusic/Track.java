package cse110.team19.flashbackmusic;

import java.util.Calendar;

public class Track {
    private Calendar cal;
    private String trackName;
    private String artist;
    private int trackNumber;
    private int score;
    private int status;

    /**
     * Constructor.
     * @param trackName name of the song
     * @param trackNumber track number of the song
     * @param artist artist of the song
     */
    public Track(String trackName, String trackNumber, String artist) {
        Calendar cal;
        this.trackName = trackName;
        this.trackNumber = Integer.parseInt(trackNumber);
        this.artist = artist;
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

    //Update the last played up the song
    public void justPlayed()
    {
        cal = Calendar.getInstance();
    }

    //Get the date and time this song was last played
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

    /* Getters and setters */
    public void setScore(int score) {
        this.score = score;
    }

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
}



