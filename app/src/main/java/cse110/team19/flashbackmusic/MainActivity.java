package cse110.team19.flashbackmusic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

/**
 * Created by Meeta on 3/6/18.
 */

public class MainActivity extends AppCompatActivity {
    //region Variables
    private boolean normalMode;
    private MusicPlayer musicPlayer;

    private PlayList playList;
    static LinkedList<Track> recentlyPlayed;

    // UI stuff
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private Adapter adapter;

    private Download download;
    private MusicController controller;
    //endregion

    // for recording location at onset of flashback mode
    GPSTracker gpsTracker;
    Location startingLocation;
    private Intent locationIntent;
    private BroadcastReceiver broadcastReceiver;

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
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //This needs to go before the button
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // UI stuff
        toolbar = (Toolbar) findViewById(R.id.nav_action);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        MediaPlayer mediaPlayer = new MediaPlayer();
        musicPlayer = new MusicPlayer(this, mediaPlayer);

        // set up recycler view
        playList = new PlayList(recentlyPlayed);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        adapter = new Adapter(playList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        musicPlayer.loadSongs();

        // Check mode and switch
        switchModes(null);

        // music url
        //Uri music_uri = Uri.parse("http://soundbible.com/grab.php?id=2191&type=zip");
        Uri music_uri = Uri.parse("http://soundbible.com/grab.php?id=2191&type=mp3");
        DownloadManager dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        download = new Download(dm, this);
        download.downloadData(music_uri);

        musicPlayer = new MusicPlayer(new MediaPlayer());

        Log.d("Download directory", Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()
                + getResources().getString(R.string.download_folder));
        PlayList playList = new PlayList(this,
                Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()
                        + getResources().getString(R.string.download_folder));
        if (normalMode) {
            playList.createNormalPlayList();
        } else {
            playList.createVibePlayList();
        }

        // Initialize the library list
        ListView listView = findViewById(R.id.listView);
        PlayListAdapter adapter = new PlayListAdapter(this, playList);
        listView.setAdapter(adapter);

        // Set up the MVC controller
        controller = new MusicController(this, adapter, musicPlayer);

        // initializing location services on start up
        gpsTracker = new GPSTracker(this);
        if (gpsTracker.permissionRequest()) {
            locationIntent = new Intent(getApplicationContext(), GPSTracker.class);
            startService(locationIntent);
        }

        registerReceiver(m_timeChangedReceiver, s_intentFilter);
    }


    /**
     * Click listener for the play button at the bottom of the activity.
     */
    @SuppressLint("NewApi")
    @Override
    public void onStop() {
        super.onStop();
        if (isChangingConfigurations() && musicPlayer.isPlaying()) {
            ; //"do nothing"
        }
    }

    public void playMusic(View view) {
        //Check if something is already playing
        if (musicPlayer.isPlaying()) {
            musicPlayer.pause();
            controller.changePause();
        } else {
            //Since there is already a song loaded, just resume the song
            musicPlayer.start();
            controller.changePlay();
        }
    }

    /**
     * Click listener fo the reset button at the bottom of the activity.
     * @param view
     */
    public void resetMusic(View view) {
        musicPlayer.resetMusic();
    }


    /**
     * Switch modes (Normal to Vibe or Vibe to Normal)
     * @param view
     */
    @SuppressLint("NewApi")
    public void switchModes(View view) {
        if(musicPlayer != null && musicPlayer.isPlaying()) {
            musicPlayer.stop();
        }

        //Get mode
        SharedPreferences sharedPreferences = this.getSharedPreferences("mode", MODE_PRIVATE);
        String mode = sharedPreferences.getString("mode", null);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (mode != null) {
            if (mode.equals(getResources().getString(R.string.mode_normal))) {
                editor.putString("mode", getResources().getString(R.string.mode_vibe));
                Button modeSwitch = (Button) findViewById(R.id.flashbackButton);
                modeSwitch.setText("N");
                normalMode = false;
                // TODO: automatically play vibe songs
            } else if (mode.equals(getResources().getString(R.string.mode_vibe))) {
                editor.putString("mode", getResources().getString(R.string.mode_normal));
                Button modeSwitch = (Button) findViewById(R.id.flashbackButton);
                modeSwitch.setText("V");
                normalMode = true;
            }
        } else {
            editor.putString("mode", getResources().getString(R.string.mode_normal));
            Button modeSwitch = (Button) findViewById(R.id.flashbackButton);
            modeSwitch.setText("V");
        }

        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(m_timeChangedReceiver);

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }

        stopService(locationIntent);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                    123);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 123: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO: is this necessary?
                    //musicPlayer.loadSongs();
                } else {

                    checkPermission();
                }
                return;
            }
            // for location service permissions
            case 100: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED
                        || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    gpsTracker.permissionRequest();
                }
            }
        }
    }

    // for registering and un-registering a broadcast receiver (prevents memory leads)
    @Override
    protected void onResume() {
        super.onResume();
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // TODO: This is where we get the information from the GPSTracker
                    // TODO: Should be constantly updating playlist
                    //textview.append("\n" + intent.getExtras().get("Coordinates"));
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("Location Updated"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
