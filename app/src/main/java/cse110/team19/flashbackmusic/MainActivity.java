package cse110.team19.flashbackmusic;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;


import static android.os.Environment.DIRECTORY_DOWNLOADS;

/**
 * Created by Meeta on 3/6/18.
 */

public class MainActivity extends AppCompatActivity {
    //region Variables
    private boolean normalMode;
    private MusicPlayer musicPlayer;
    private Download download;
    private MusicController controller;
    //endregion

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
        setContentView(R.layout.activity_main_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Check mode and switch
        switchModes(null);

        // music url
        //Uri music_uri = Uri.parse("http://soundbible.com/grab.php?id=2191&type=zip");
        Uri music_uri = Uri.parse("http://soundbible.com/grab.php?id=2191&type=mp3");
        DownloadManager dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        download = new Download(dm, getResources().getString(R.string.download_folder));
        download.downloadData(music_uri);

        musicPlayer = new MusicPlayer(new MediaPlayer());

        Log.d("Download directory", Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()
                + getResources().getString(R.string.download_folder));
        PlayList playList = new PlayList(this,
                Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()
                        + getResources().getString(R.string.download_folder));
        if(normalMode) {
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

        registerReceiver(m_timeChangedReceiver, s_intentFilter);
    }


    /**
     * Click listener for the play button at the bottom of the activity.
     * @param view
     */
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
        }
    }
}
