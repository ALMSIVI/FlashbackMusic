package cse110.team19.flashbackmusic;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.location.Location;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Meeta on 3/6/18.
 */

public class MusicPlayer {
    private MediaPlayer mediaPlayer;
    private Context context;
    private ArrayList<Integer> audioResourceId = new ArrayList<Integer>();
    private List<Track> trackList = new ArrayList<Track>();

    /**
     * Constructor
     * @param c Context
     * @param mp MediaPlayer
     */
    public MusicPlayer(Context c, MediaPlayer mp) {
        context = c;
        mediaPlayer = mp;


        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
    }

    /**
     * Plays current song.
     */
    public void start() {
        mediaPlayer.start();
    }

    /**
     * Pauses playing song.
     */
    public void pause() {
        mediaPlayer.pause();
    }

    /**
     * Stops the songs.
     */
    public void stop() {
        mediaPlayer.stop();
    }

    /**
     * Checks if player is playing.
     * @return true - playing
     *         false - not playing
     */
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    /**
     * Reset music.
     *
     * @param view
     */
    public void resetMusic(View view) {
        mediaPlayer.seekTo(0);
    }

    /**
     * Get track list for Flashback Mode
     * @return trackList
     */
    public List<Track> getTrackList() {
        return trackList;
    }

    /**
     * Get media player.
     * @return mediaPlayer
     */
    public MediaPlayer getPlayer() {
        return mediaPlayer;
    }

    /**
     * Load the songs.
     */
    public void loadSongs() {
        final Field[] fields = R.raw.class.getFields(); //Gets the all the files (tracks) in raw folder
        for (int count = 0; count < fields.length; count++) { //Goes through each track
            String name = fields[count].getName();

            //Gets id to play the track (used in LoadMedia())
            int resourceID = context.getResources().getIdentifier(name, "raw", context.getPackageName());
            audioResourceId.add(resourceID);

            //Gets the metadata of the track (album, artist, track number in album, track name)
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            Resources res = context.getResources();
            AssetFileDescriptor afd = res.openRawResourceFd(resourceID);
            mmr.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
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
            int numTracks = 0;
            if (trackNumber != null) {
                String[] numbers = trackNumber.split("/");
                trackNo = Integer.parseInt(numbers[0]);
                numTracks = Integer.parseInt(numbers[1]);
            }
            if (trackName == null || trackName.equals("")) {
                trackName = "Unknown track";
            }

            // Create the track
            Track t = new Track(trackName, albumName, artist, trackNo, resourceID);

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
            if (!loc.equals("Unknown Location")) {
                String[] locationValue = loc.split("");
                double latitude = Double.parseDouble(locationValue[0]);
                double longitude = Double.parseDouble(locationValue[1]);
                Location location = new Location("");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                t.setLocation(location);
            }
        }
    }

    public void createFlashback() {
        final Map<Integer, Track> tempMap = new TreeMap<>();

        Calendar calender;
        calender = Calendar.getInstance();
        int currentHour = calender.get(Calendar.HOUR_OF_DAY);
        int currentDay = calender.get(Calendar.DAY_OF_WEEK);
        String timeOfDay = currentTime(currentHour);

            //For each track
            for (Track track : trackList) {
                // TODO: Criterion check

                //Get status
                int status = track.getStatus();

                if (status == 1) {
                    track.incrementScore(100);
                } else if (status == -1) {
                    track.makeScoreNegative();
                }

                //To insert into the tree map
                if(track.getScore() > -1)
                {
                    //Check if there is a tie
                    while(tempMap.containsKey(track.getScore()))
                    {
                        Track temp = tempMap.get(track.getScore());
                        if(track.getTimeSinceLastPlayed() > temp.getTimeSinceLastPlayed())
                        {
                            track.incrementScore(1);
                        }
                    }

                    tempMap.put(track.getScore(), track);
                }
            }

        for(Map.Entry<Integer, Track> entry : tempMap.entrySet()){
            Track toInsert = entry.getValue();
            trackList.add(toInsert);
        }
    }

    public String currentTime(int hour) {
        if (5 <= hour && hour < 11) {
            return "morning";
        }

        if (11 <= hour && hour < 17) {
            return "afternoon";
        } else {
            return "evening";
        }
    }
}
