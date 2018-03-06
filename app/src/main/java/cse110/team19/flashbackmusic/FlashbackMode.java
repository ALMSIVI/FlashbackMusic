package cse110.team19.flashbackmusic;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

public class FlashbackMode extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private List<Track> list = new ArrayList<Track>();


    private ArrayList<Integer> audioResourceId = new ArrayList<Integer>();
    static LinkedList<Track> recentlyPlayed;

    // for the LibraryAdaptor
    private List<Album> album_list = new ArrayList<Album>();
    private Map<Album, List<Track>> album_to_tracks = new LinkedHashMap<Album, List<Track>>();

    // for recording location at onset of flashback mode
    GPSTracker gpsTracker;
    Location startingLocation;

    // Monitors time change
    private static IntentFilter s_intentFilter;

    static {
        s_intentFilter = new IntentFilter();
        s_intentFilter.addAction(Intent.ACTION_TIME_TICK);
        s_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        s_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
    }

    private final BroadcastReceiver m_timeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(Intent.ACTION_TIME_CHANGED)) {
                // TODO: Update playlist based on time and day
            }
        }
    };

    /**
     * Initialize the activity.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //This needs to go before the button
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashback_mode);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        gpsTracker = new GPSTracker(this);
        startingLocation = gpsTracker.getLocation();

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        loadSongs();
        createFlashback(album_to_tracks);

        ListView playList = findViewById(R.id.playList);
        playList.setAdapter(new PlayListAdapter(this, list, mediaPlayer));

        registerReceiver(m_timeChangedReceiver, s_intentFilter);
    }

    public void createFlashback(Map<Album, List<Track>> input) {
        final Map<Integer, Track> tempMap = new TreeMap<>();

        Calendar calender;
        calender = Calendar.getInstance();
        int currentHour = calender.get(Calendar.HOUR_OF_DAY);
        int currentDay = calender.get(Calendar.DAY_OF_WEEK);
        String timeOfDay = currentTime(currentHour);

        for (Map.Entry<Album, List<Track>> entry : input.entrySet()) {
            //This is the list of tracks
            List<Track> currentList = entry.getValue();

            //For each track
            for (Track track : currentList) {
                // Check time of day
                if (track.getTimePlayed() != null && track.getTimePlayed().equals(timeOfDay)) {
                    track.incrementScore(500);
                }

                //Check day of week
                if (track.getDayPlayed() > -1 && track.getDayPlayed() == (currentDay)) {
                    track.incrementScore(500);
                }

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
        }

        for(Map.Entry<Integer, Track> entry : tempMap.entrySet()){
            Track toInsert = entry.getValue();
            list.add(toInsert);
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

    public void resetMusic(View view) {
        mediaPlayer.seekTo(0);
    }

    public void playMusic(View view) {
        Button playButton = (Button) findViewById(R.id.playButton);
        //Check if something is already playing
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Drawable play = getResources().getDrawable(R.drawable.ic_play_arrow_actuallyblack_24dp);
            playButton.setCompoundDrawablesWithIntrinsicBounds(null, play, null, null);
        } else {
            //Since there is already a song loaded, just resume the song
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }

            Drawable pause = getResources().getDrawable(R.drawable.ic_pause_actuallyblack_24dp);
            playButton.setCompoundDrawablesWithIntrinsicBounds(null, pause, null, null);
        }
    }

    /**
     * Load the songs.
     */
    public void loadSongs() {
        Map<String, Album> album_data = new LinkedHashMap<String, Album>();
        final Field[] fields = R.raw.class.getFields(); //Gets the all the files (tracks) in raw folder
        for (int count = 0; count < fields.length; count++) { //Goes through each track
            String name = fields[count].getName();

            //Gets id to play the track (used in LoadMedia())
            int resourceID = getResources().getIdentifier(name, "raw", getPackageName());
            audioResourceId.add(resourceID);

            //Gets the metadata of the track (album, artist, track number in album, track name)
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            Resources res = getResources();
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
            Track t = new Track(trackName, trackNo, artist, resourceID);
            if (!album_data.containsKey(albumName)) {
                Album newAlbum = new Album(albumName, artist, numTracks);
                album_data.put(albumName, newAlbum);
                album_list.add(newAlbum);
                newAlbum.addTrack(t);

                // update data to be sent to adaptor
                List<Track> tracks = new LinkedList<Track>();
                tracks.add(t);
                album_to_tracks.put(newAlbum, tracks);
            } else {
                album_data.get(albumName).addTrack(t);
                album_to_tracks.get(album_data.get(albumName)).add(t);
            }

            // Retrieve data from sharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("track_info", MODE_PRIVATE);
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

    public void switchNormal(View view) {
        // update sharedPreferences
        mediaPlayer.stop();
        SharedPreferences sharedPreferences = getSharedPreferences("mode", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("mode", "Normal");
        editor.apply();
        // Finish the task
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(m_timeChangedReceiver);
    }
}