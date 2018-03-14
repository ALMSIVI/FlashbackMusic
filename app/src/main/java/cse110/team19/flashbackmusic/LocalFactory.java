package cse110.team19.flashbackmusic;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by YueWu on 3/12/2018.
 */

public class LocalFactory implements TrackFactory {
    private Context context;

    public LocalFactory(Context context) {
        this.context = context;
    }

    /**
     * Creates a track from the download path.
     * @param path The downloaded songs directory.
     * @return A local song.
     */
    public Track createTrack(String path) {
        //Gets the metadata of the track (album, artist, track number in album, track name)
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

        String pathName = path;
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
        Track track = new Track(trackName, albumName, artist, trackNo, pathName);


        // Retrieve data from sharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("track_info", MODE_PRIVATE);
        int status = sharedPreferences.getInt(track.getPathName() + "Status", 0);
        track.setStatus(status);

        // calendar
        String cal = sharedPreferences.getString(track.getPathName() + "Time", null);
        if (cal != null) {
            try {
                // TODO: Firebase
                //Calendar calendar = Calendar.getInstance();
                //SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                //calendar.setTime(format.parse(cal));
                //track.setCalendar(calendar);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // location
        String loc = sharedPreferences.getString(track.getPathName() + "Location", "Unknown Location");
            /*if (!loc.equals("Unknown Location")) {
                String[] locationValue = loc.split("");
                double latitude = Double.parseDouble(locationValue[0]);
                double longitude = Double.parseDouble(locationValue[1]);
                Location location = new Location("");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                t.setLocation(location);
            }*/
        return track;
    }
}
