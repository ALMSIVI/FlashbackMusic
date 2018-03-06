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
    private MusicPlayer musicPlayer;
    //private List<Track> list = new ArrayList<Track>();


    //private ArrayList<Integer> audioResourceId = new ArrayList<Integer>();
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

        if (musicPlayer == null) {
            musicPlayer = new MusicPlayer(this, new MediaPlayer());
        }

        musicPlayer.loadSongs();
        musicPlayer.createFlashback();

        ListView playList = findViewById(R.id.playList);
        playList.setAdapter(new PlayListAdapter(this, musicPlayer.getTrackList(), musicPlayer.getPlayer()));

        registerReceiver(m_timeChangedReceiver, s_intentFilter);
    }



    public void playMusic(View view) {
        Button playButton = (Button) findViewById(R.id.playButton);
        //Check if something is already playing
        if (musicPlayer != null && musicPlayer.isPlaying()) {
            musicPlayer.pause();
            Drawable play = getResources().getDrawable(R.drawable.ic_play_arrow_actuallyblack_24dp);
            playButton.setCompoundDrawablesWithIntrinsicBounds(null, play, null, null);
        } else {
            //Since there is already a song loaded, just resume the song
            if (musicPlayer != null) {
                musicPlayer.start();
            }

            Drawable pause = getResources().getDrawable(R.drawable.ic_pause_actuallyblack_24dp);
            playButton.setCompoundDrawablesWithIntrinsicBounds(null, pause, null, null);
        }
    }

    public void switchNormal(View view) {
        // update sharedPreferences
        musicPlayer.stop();
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