package cse110.team19.flashbackmusic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.*;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import java.util.*;


/**
 * This is the normal mode window.
 */
public class NormalMode extends AppCompatActivity {
    /* Members */
    // player
    private MusicPlayer musicPlayer;
    //private ArrayList<Integer> audioResourceId = new ArrayList<Integer>();
    static LinkedList<Track> recentlyPlayed;

    // for the LibraryAdaptor
    //private List<Album> album_list = new ArrayList<Album>();
    //private Map<Album, List<Track>> album_to_tracks = new LinkedHashMap<Album, List<Track>>();

    /* Methods */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPreferences = getSharedPreferences("mode", MODE_PRIVATE);

        String mode = sharedPreferences.getString("mode", "");
        if (mode.equals("Flashback")) {
            switchFlashback(null);
        }


        // Initialize the media player and load songs
        if (musicPlayer == null) {
            musicPlayer = new MusicPlayer(this, new MediaPlayer());
        }

        musicPlayer.loadSongs();

        // Initialize the library list
        ListView listView = findViewById(R.id.expandableListView);
        listView.setAdapter(new PlayListAdapter(this, musicPlayer.getTrackList(), musicPlayer);
    }

    /**
     * onStop
     */
    @Override
    public void onStop() {
        super.onStop();
        if (isChangingConfigurations() && musicPlayer.isPlaying()) {
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

    /**
     * Switch to Flashback mode.
     * @param
     */
    public void switchFlashback(View view) {
        if(musicPlayer != null && musicPlayer.isPlaying()) {
            musicPlayer.stop();
        }
        // Change the mode in sharedpreferences
        SharedPreferences sharedPreferences = getSharedPreferences("mode", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("mode", "Flashback");
        editor.apply();

        // Create the intent and switch activity
        Intent intent = new Intent(this, FlashbackMode.class);
        startActivity(intent);
    }
}