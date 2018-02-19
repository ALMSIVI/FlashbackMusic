package cse110.team19.flashbackmusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //This needs to go before the button
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashback_mode);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadSongs();
        //TODO: add this line - createFlashback(album_to_tracks);
        createFlashback(album_to_tracks);
        Log.d("list size", list.size() + "");
        mediaPlayer = new MediaPlayer();
        ListView playList = findViewById(R.id.playList);
        playList.setAdapter(new PlayListAdapter(this, list, mediaPlayer));

        registerReceiver(m_timeChangedReceiver, s_intentFilter);
    }

    public void switchNormal(View view) {
        // update sharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("mode", "Normal");
        editor.apply();
        // Finish the task
        finish();
    }

    public void createFlashback(Map<Album, List<Track>> input) {
        final ArrayList<Track> tempMap = new ArrayList<Track>();

        Calendar calender;
        calender = Calendar.getInstance();
        int currentHour = calender.get(Calendar.HOUR_OF_DAY);
        int currentDay = calender.get(Calendar.DAY_OF_WEEK);
        String timeOfDay = currentTime(currentHour);

        //For each album
        for(Map.Entry<Album, List<Track>> entry : input.entrySet()) {
            //This is the list of tracks
            List<Track> currentList = entry.getValue();

            //For each track
            for (Track track : currentList) {
                Log.d("track name woo", track.getTrackName());
                //TODO: MAKE SURE THIS CODE IS UNCOMMENTED AND WORKS!!! Check time of day
                //TODO: This was causing a null pointer exception :)
                /*if(track.getTimePlayed() != null && track.getTimePlayed().equals(timeOfDay)) {
                    track.incrementScore(5);
                }

                //Check day of week
                if(track.getDayPlayed() > -1 && track.getDayPlayed() == (currentDay)) {
                    track.incrementScore(5);
                }*/

                //Get status
                int status = track.getStatus();

                if(status == 1) {
                    track.incrementScore(1);
                } else if(status == -1) {
                    track.makeScoreNegative();
                }
                Log.d("track score", track.getScore() + "");
                if((track.getScore() >= 0)) {
                    //tempMap.put(track.getScore(), track);
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

        for (Track t: tempMap) {
            Track toInsert = t;
            list.add(toInsert);
        }
    }

    public String currentTime(int hour) {
        if( 5 <= hour && hour < 11) {
            return "morning";
        }

        if( 11 <= hour && hour < 17 ) {
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
        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
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
            Log.d("album name", albumName);
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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(m_timeChangedReceiver);
    }
}
