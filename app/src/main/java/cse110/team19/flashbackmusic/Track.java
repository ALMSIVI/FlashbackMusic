package cse110.team19.flashbackmusic;

import android.location.Location;

import com.google.android.gms.plus.model.people.Person;

import java.util.*;

public class Track {
    private Calendar cal;
    private String trackName;
    private String artistName;
    private String albumName;
    private int trackNumber;
    private int score;
    private int status;
    private String website;
    private ArrayList<Location> locations;
    private Person lastPlayedBy;
    private long time;
    private String personLastPlayed;
    private String pathName;

    /**
     * Constructor.
     * @param trackName name of the song
     * @param trackNumber track number of the song
     * @param artist artistName of the song
     */
    public Track(String trackName, String albumName, String artist, int trackNumber, String pathName) {
        this.trackName = trackName;
        this.albumName = albumName;
        this.artistName = artist;
        this.trackNumber = trackNumber;
        this.pathName = pathName;
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

    //region Getters
    // Get info for flashback
    public Date getTime() {
        if (cal == null) { // not implemented
            return null;
        } else {
            return cal.getTime();
        }
    }



    //Get time since last play
    public long getTimeSinceLastPlayed() {
        return time;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getAlbumName() {
        return albumName;
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

    public String getPersonLastPlayed() {
        return personLastPlayed;
    }

    public String getWebsite() {
        return website;
    }

    public String getPathName() {
        return pathName;
    }
    //endregion

    //region Setters
    public void setCalendar(Calendar calendar) {
        cal = calendar;
    }

    public void setLocation(Location location) {
        this.locations.add(location);
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setPersonLastPlayed(String name) {
        this.personLastPlayed = name;
    }

    public void setWebsite(String site) {
        this.website = site;
    }

    public void setPathName(String path) {
        this.pathName = path;
    }
    //endregion

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
        locations.add(location);
        Set<Location> parser = new HashSet<>();
        parser.addAll(locations);
        locations.clear();
        locations.addAll(parser);
        this.time = time;
    }

    /* Comparators for Track */
    public static Comparator<Track> recentComparator = new Comparator<Track>() {
        @Override
        public int compare(Track t1, Track t2) {
            if (t1 == null || t2 == null) {
                return 1;
            }
            return t1.getTime().compareTo(t2.getTime()) != 0 ?
            t1.getTime().compareTo(t2.getTime()) :
            t1.getTrackName().compareTo(t2.getTrackName());
        }
    };

    public static Comparator<Track> favoriteComparator = new Comparator<Track>() {
        @Override
        public int compare(Track t1, Track t2) {
            if (t1 == null || t2 == null) {
                return 1;
            }
            return t1.getStatus() != t2.getStatus() ?
                    t1.getStatus() - t2.getStatus() :
                    t1.getTrackName().compareTo(t2.getTrackName());
        }
    };

    public static Comparator<Track> nameComparator = new Comparator<Track>() {
        @Override
        public int compare(Track t1, Track t2) {
            if (t1 == null || t2 == null) {
                return 1;
            }
            return t1.getTrackName().compareTo(t2.getTrackName());
        }
    };

    public static Comparator<Track> albumComparator = new Comparator<Track>() {
        @Override
        public int compare(Track t1, Track t2) {
            if (t1 == null || t2 == null) {
                return 1;
            }
            return t1.getAlbumName().compareTo(t2.getAlbumName()) != 0 ?
                    t1.getAlbumName().compareTo(t2.getAlbumName()):
                    t1.getTrackName().compareTo(t2.getTrackName());
        }
    };

    public static Comparator<Track> artistComparator = new Comparator<Track>() {
        @Override
        public int compare(Track t1, Track t2) {
            if (t1 == null || t2 == null) {
                return 1;
            }
            return t1.getArtistName().compareTo(t2.getArtistName()) != 0 ?
                    t1.getArtistName().compareTo(t2.getArtistName()):
                    t1.getTrackName().compareTo(t2.getTrackName());
        }
    };

    public static Comparator<Track> scoreComparator = new Comparator<Track>() {
        @Override
        public int compare(Track t1, Track t2) {
            if (t1 == null || t2 == null) {
                return 1;
            }
            return t1.getScore() != t2.getScore() ?
                    t1.getScore() - t2.getScore() :
                    t1.getTrackName().compareTo(t2.getTrackName());
        }
    };
}