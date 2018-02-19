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
    private Track isPlaying;
    private int audioIndex = 0;
    private Date time;

    private ArrayList<Integer> audioResourceId = new ArrayList<Integer>();
    static LinkedList<Track> recentlyPlayed;

    // for the LibraryAdaptor
    private List<Album> album_list = new ArrayList<Album>();
    private Map<Album, List<Track>> album_to_tracks = new LinkedHashMap<Album, List<Track>>();

    // for recording location of song
    Geocoder geocoder;
    List<Address> addresses;
    GPSTracker gpstracker;
    Location location;


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

        loadSongs();
        createFlashback(album_to_tracks);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {

                        location = gpstracker.getLocation();
                        isPlaying.updateInfo(location, time.getTime());

                        saveTrackInfo(true, isPlaying);

                        if(audioResourceId.size() > audioIndex) {
                            loadMedia(audioResourceId.get(audioIndex));
                        }
                        else{
                            changePausePlay();
                            updateText();
                        }
                    }
                }
        );

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });

        ListView playList = findViewById(R.id.playList);
        playList.setAdapter(new PlayListAdapter(this, list, mediaPlayer));

        registerReceiver(m_timeChangedReceiver, s_intentFilter);

        // Begin playing the songs
        for (Track t : list) {
            int id = t.getResourceId();
            audioResourceId.add(id);
            Log.d("trackname", t.getTrackName());
            Log.d("track number", t.getTrackNumber() + "");
            mediaPlayer.reset();
            AssetFileDescriptor assetFileDescriptor = getResources().openRawResourceFd(id);
            try {
                mediaPlayer.setDataSource(assetFileDescriptor);
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }

        isPlaying = list.get(0);
        loadMedia(audioResourceId.get(audioIndex));
        playMusic(null);
    }

    public void createFlashback(Map<Album, List<Track>> input) {
        final ArrayList<Track> tempMap = new ArrayList<Track>();

        Calendar calender;
        calender = Calendar.getInstance();
        int currentHour = calender.get(Calendar.HOUR_OF_DAY);
        int currentDay = calender.get(Calendar.DAY_OF_WEEK);
        String timeOfDay = currentTime(currentHour);

        //For each album
        for (Map.Entry<Album, List<Track>> entry : input.entrySet()) {
            //This is the list of tracks
            List<Track> currentList = entry.getValue();

            //For each track
            for (Track track : currentList) {
                // Check time of day
                if (track.getTimePlayed() != null && track.getTimePlayed().equals(timeOfDay)) {
                    track.incrementScore(5);
                }

                //Check day of week
                if (track.getDayPlayed() > -1 && track.getDayPlayed() == (currentDay)) {
                    track.incrementScore(5);
                }

                //Get status
                int status = track.getStatus();

                if (status == 1) {
                    track.incrementScore(1);
                } else if (status == -1) {
                    track.makeScoreNegative();
                }
                Log.d("track score", track.getScore() + "");
                if ((track.getScore() >= 0)) {
                    tempMap.add(track);
                }
            }
        }

        // Sort the tracks based on track number
        Collections.sort(tempMap, new Comparator<Track>() {
            @Override
            public int compare(Track t1, Track t2) {
                return t2.getScore() - t1.getScore();
            }
        });

        for (Track t : tempMap) {
            Track toInsert = t;
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
            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            if (cal != null) {
                try {
                    Calendar calendar = Calendar.getInstance();
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

    public void loadMedia(int resourceId) {
        mediaPlayer.reset();
        AssetFileDescriptor assetFileDescriptor = getResources().openRawResourceFd(resourceId);
        try {
            mediaPlayer.setDataSource(assetFileDescriptor);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        updateText();

        audioIndex++;
    }

    private void updateText() {
        // Update the "Now playing" text
        TextView infoView = findViewById(R.id.info);
        infoView.setText(isPlaying.getTrackName());
        // Update the "Last played" text
        TextView lastPlayedView = findViewById(R.id.lastPlayed);
        if (isPlaying.getTime() == null) {
            lastPlayedView.setText(getString(R.string.never_played_info));
        } else {
            String lastLocation = "Unknown location";

            if (location != null) {
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                lastLocation = addresses.get(0).getFeatureName();
            }

            String lastPlayedInfo = String.format(getString(R.string.last_played_info),
                    isPlaying.getTime(), lastLocation);
            lastPlayedView.setText(lastPlayedInfo);
        }
    }

    public void switchNormal(View view) {
        // update sharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("mode", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("mode", "Normal");
        editor.apply();
        // Finish the task
        finish();
    }

    private void saveTrackInfo(boolean all, Track track) {
        SharedPreferences sharedPreferences = getSharedPreferences("track_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(track.getTrackName() + "Status", track.getStatus());

        if(all) {
            editor.putString(track.getTrackName() + "Time", track.getTime());
            editor.putString(track.getTrackName() + "Location", track.getLocation());
        }
        editor.apply();
    }

    private void changePausePlay() {
        Button mainPauseButton = (Button)findViewById(R.id.playButton);
        Drawable play = getResources().getDrawable(R.drawable.ic_play_arrow_actuallyblack_24dp);
        mainPauseButton.setCompoundDrawablesWithIntrinsicBounds(null, play, null, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(m_timeChangedReceiver);
    }
}
