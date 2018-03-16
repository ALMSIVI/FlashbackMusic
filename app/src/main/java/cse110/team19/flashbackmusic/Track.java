package cse110.team19.flashbackmusic;

import android.location.Location;

import com.google.android.gms.plus.model.people.Person;

import java.time.LocalDateTime;
import java.util.*;

public class Track {
    private String trackName; // Firebase
    private String artistName;
    private String albumName;
    private int trackNumber;
    private int score;
    private int status; //SharedPreferences
    private String website; // Firebase
    private String personLastPlayed;
    private String pathName;

    private LocalDateTime date; // Firebase
    private Location location; // Firebase

    /**
     * Default constructor.
     */
    public Track() {}

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
        location = new Location("");
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
    /**
     * Date is stored into Firebase.
     * @return date this track was last played
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * TrackName is stored into Firebase.
     * @return name of the track
     */
    public String getTrackName() {
        return trackName;
    }

    /**
     * website is stored into Firebase.
     * @return url of the song
     */
    public String getWebsite() {
        return website;
    }

    /**
     * location is stored into Firebase.
     * @return location of the long
     */
    public Location getLocation() {
        return location;
    }

    /**
     * personLastPlayed is stored into Firebase.
     * @return the person who last played th song
     */
    public String getPersonLastPlayed() {
        return personLastPlayed;
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

    public String getPathName() {
        return pathName;
    }

    //endregion

    //region Setters
    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setWebsite(String site) {
        this.website = site;
    }

    public void setPathName(String path) {
        this.pathName = path;
    }

    public void setLocation(double latitude, double longitude) {
        location.setLatitude(latitude);
        location.setLongitude(longitude);
    }

    public void setDate(int year, int month, int day, int hour, int minute) {
        this.date = LocalDateTime.of(year, month, day, hour, minute);
    }

    public void setPerson(String id) {
        personLastPlayed = id;
    }

    //endregion

    public void updateInfo(Location newLocation, LocalDateTime newDate, User user) {
        location = newLocation;
        date = newDate;
        if (user != null) {
            personLastPlayed = user.getId();
        }
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

    /* Comparators for Track */
    public static Comparator<Track> recentComparator = new Comparator<Track>() {
        @Override
        public int compare(Track t1, Track t2) {
            if (t1 == null || t2 == null) {
                return 1;
            }else if (t1.getDate() == null && t2.getDate() != null) {
                return -1;
            } else if (t2.getDate() == null && t1.getDate() != null) {
                return 1;
            } else if (t1.getDate() == null && t2.getDate() == null) {
                return t1.getTrackName().compareTo(t2.getTrackName());
            } else {
                return t1.getDate().compareTo(t2.getDate()) != 0 ?
                        t1.getDate().compareTo(t2.getDate()) :
                        t1.getTrackName().compareTo(t2.getTrackName());
            }
        }
    };

    public static Comparator<Track> favoriteComparator = new Comparator<Track>() {
        @Override
        public int compare(Track t1, Track t2) {
            if (t1 == null || t2 == null) {
                return 1;
            }
            // Reverse logic: the HIGHER the status is, the LOWER index it is in the list.
            return t1.getStatus() != t2.getStatus() ?
                    t2.getStatus() - t1.getStatus() :
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
                    t1.getTrackNumber() - t2.getTrackNumber();
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
                    t2.getScore() - t1.getScore() :
                    t1.getTrackName().compareTo(t2.getTrackName());
        }
    };
}