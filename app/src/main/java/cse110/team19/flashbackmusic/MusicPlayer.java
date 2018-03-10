package cse110.team19.flashbackmusic;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

/**
 * Created by Meeta on 3/6/18.
 */

public class MusicPlayer {
    private MediaPlayer mediaPlayer;
    private Context context;
    private ArrayList<Integer> audioResourceId = new ArrayList<Integer>();
    private List<Track> trackList = new ArrayList<Track>();

    // TODO: Is this useful anymore?
    // private int audioIndex;

    // MVC
    private MusicController controller;

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

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (isPlaying()) {
                    controller.updateTrackInfo();
                    controller.saveTrackInfo(true, controller.getIsPlaying());
                    if (audioResourceId.size() > 0 /*audioIndex */) {
                        //TODO: fix this
                        // loadMedia(audioResourceId.get(audioIndex).first,
                        //        audioResourceId.get(audioIndex).second);
                    } else {
                        controller.changePlay();
                        controller.updateText();
                    }
                }
            }
        });

        /*
        // Load the songs to the player
        audioResourceId = new ArrayList<Integer>();
        //changePlay();
        for (Track t : trackList) {
            Log.d("audioIndex", audioIndex + "");
            int id = t.getResourceId();
            audioResourceId.add(id);
            Log.d("trackname", t.getTrackName());
            Log.d("track number", t.getTrackNumber() + "");
            AssetFileDescriptor assetFileDescriptor = context.getResources().openRawResourceFd(id);
            try {
                mediaPlayer.setDataSource(assetFileDescriptor);
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }

        if (audioResourceId.size() > audioIndex) {
            loadMedia(audioResourceId.get(audioIndex).first, audioResourceId.get(audioIndex).second);
        }
        */
    }

    public void setController(MusicController controller) {
        this.controller = controller;
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
     */
    public void resetMusic() {
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
     * Load the songs.
     */
    public void loadSongs() {
        // getting songs out of Downloads folder
        String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).toString() + "/DownloadedSongs";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] fields = directory.listFiles();
        Log.d("Files", "Size: " + fields.length);
        for (int i = 0; i < fields.length; i++) {
            Log.d("Files", "FileName:" + fields[i].getName());
        }

        //final Field[] fields = R.raw.class.getFields(); //Gets the all the files (tracks) in raw folder
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
            trackList.add(t);

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
    }

    public void createFlashback() {
        trackList = new ArrayList<Track>();

        Calendar calender;
        calender = Calendar.getInstance();

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

                if(track.getScore() > -1) {
                    trackList.add(track);
                }
            }

        Collections.sort(trackList, Track.scoreComparator);
    }
}
