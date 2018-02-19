package cse110.team19.flashbackmusic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.*;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This is the normal mode window.
 */
public class NormalMode extends AppCompatActivity {
    /* Members */
    // player
    private MediaPlayer mediaPlayer;
    private ArrayList<Integer> audioResourceId = new ArrayList<Integer>();
    static LinkedList<Track> recentlyPlayed;

    // for the LibraryAdaptor
    private List<Album> album_list = new ArrayList<Album>();
    private Map<Album, List<Track>> album_to_tracks = new LinkedHashMap<Album, List<Track>>();

    /* Methods */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_mode);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String mode = sharedPreferences.getString("mode", "");
        if (mode.equals("Flashback")) {
            switchFlashback(null);
        }


        // Initialize the media player and load songs
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        loadSongs();

        // Initialize the library list
        //TODO: initialize content and the list
        ExpandableListView expandableListView = findViewById(R.id.expandableListView);
        expandableListView.setAdapter(new LibraryAdapter(this, album_list, album_to_tracks, mediaPlayer));

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                if (expandableListView.isGroupExpanded(i)) {
                    expandableListView.collapseGroup(i);
                    return false;
                } else {
                    expandableListView.expandGroup(i);
                    return true;
                }
            }
        });
    }

    /**
     * onStop
     */
    @Override
    public void onStop() {
        super.onStop();
        if (isChangingConfigurations() && mediaPlayer.isPlaying()) {
            ; //"do nothing"
        }
    }

    /**
     * This is the play button's listener. When the user clicks the button, the music will be played
     * and the button will change to pause. When the button is clicked again, the music will be
     * paused and the button will change to play.
     *
     * @param view
     */
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
     * Reset music
     *
     * @param view
     */
    public void resetMusic(View view) {
        mediaPlayer.seekTo(0);
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
            // Retrieve data from sharedPreferences
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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



    /**
     * Switch to Flashback mode.
     *
     * @param
     */
    public void switchFlashback(View view) {
        if(mediaPlayer != null) {
            mediaPlayer.stop();
        }
        // Change the mode in sharedpreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("mode", "Flashback");
        editor.apply();

        // Create the intent and switch activity
        Intent intent = new Intent(this, FlashbackMode.class);
        startActivity(intent);
    }

}