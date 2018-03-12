package cse110.team19.flashbackmusic;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by YueWu on 3/6/2018.
 */

public class PlayList {
    private List<Track> playList;
    private String downloadFolder;
    private Context context;
    private int currentTrackIndex;

    public enum Sort {
        Recent, Name, Album, Artist, Favorite, Score;
    }

    Sort mode = Sort.Recent;

    public PlayList(Context cont, String folder) {
        context = cont;
        downloadFolder = folder;
        playList = new ArrayList<Track>();
    }

    public void createNormalPlayList() {
        playList.clear();

        // getting songs out of Downloads folder
        Log.d("Files", "Path: " + downloadFolder);
        File directory = new File(downloadFolder);
        File[] fields = directory.listFiles();
        Log.d("Files", "Size: " + fields.length);
        for (int i = 0; i < fields.length; i++) {
            Log.d("Files", "FileName:" + fields[i].getName());
        }

        for (int count = 0; count < fields.length; count++) { //Goes through each track

            //Gets the metadata of the track (album, artist, track number in album, track name)
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();

            String pathName = downloadFolder + "/" + fields[count].getName();
            Log.d("Retrieving Metadata", pathName);
            mmr.setDataSource(pathName);

            String albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String trackNumber = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
            String trackName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

            // Parse the metadata
            if (albumName == null || albumName.equals("")) {
                albumName = "Unknown album";
            }
            if (artist == null || artist.equals("")) {
                artist = "Unknown artist";
            }
            int trackNo = 0;
            if (trackNumber != null) {
                String[] numbers = trackNumber.split("/");
                trackNo = Integer.parseInt(numbers[0]);
            }
            if (trackName == null || trackName.equals("")) {
                trackName = "Unknown track";
            }

            // Create the track
            Track t = new Track(trackName, albumName, artist, trackNo, pathName);
            playList.add(t);

            // Retrieve data from sharedPreferences
            SharedPreferences sharedPreferences = context.getSharedPreferences("track_info", MODE_PRIVATE);
            int status = sharedPreferences.getInt(t.getTrackName() + "Status", 0);
            t.setStatus(status);

            // calendar
            String cal = sharedPreferences.getString(t.getTrackName() + "Time", null);
            if (cal != null) {
                try {
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                    calendar.setTime(format.parse(cal));
                    t.setCalendar(calendar);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // location
            String loc = sharedPreferences.getString(t.getTrackName() + "Location", "Unknown Location");
            /*if (!loc.equals("Unknown Location")) {
                String[] locationValue = loc.split("");
                double latitude = Double.parseDouble(locationValue[0]);
                double longitude = Double.parseDouble(locationValue[1]);
                Location location = new Location("");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                t.setLocation(location);
            }*/
        }

        sortRecent();
    }

    public void createVibePlayList() {
        playList.clear();
        // TODO: retrieve every song info from Firebase

        Calendar calender;
        calender = Calendar.getInstance();

        //For each track
        for (Track track : playList) {
            // TODO: Criterion check

            //Get status
            int status = track.getStatus();

            if (status == 1) {
                track.incrementScore(100);
            } else if (status == -1) {
                track.makeScoreNegative();
            }

            if(track.getScore() > -1) {
                playList.add(track);
            }
        }

        sortScore();
    }

    public Track getCurrentTrack() {
        return playList.get(currentTrackIndex);
    }

    public int size() {
        return playList.size();
    }

    public Track get(int i) {
        return playList.get(i);
    }

    public boolean isNormalMode() {
        return mode != Sort.Score;
    }

    public void sortRecent() {
        mode = Sort.Recent;
        Collections.sort(playList, Track.recentComparator);
    }

    public void sortName() {
        mode = Sort.Name;
        Collections.sort(playList, Track.nameComparator);
    }

    public void sortAlbum() {
        mode = Sort.Album;
        Collections.sort(playList, Track.albumComparator);
    }

    public void sortArtist() {
        mode = Sort.Artist;
        Collections.sort(playList, Track.artistComparator);
    }

    public void sortFavorite() {
        mode = Sort.Favorite;
        Collections.sort(playList, Track.favoriteComparator);
    }

    public void sortScore() {
        // TODO: Retrieve music from website, calculate scores, update playlist
        mode = Sort.Score;
        Collections.sort(playList, Track.scoreComparator);
    }
}
