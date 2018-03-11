package cse110.team19.flashbackmusic;

import android.Manifest;
import android.app.Dialog;
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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.apache.GoogleApacheHttpTransport;
import com.google.api.services.people.v1.PeopleScopes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Created by Meeta on 3/6/18.
 */

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{


    private boolean normalMode;

    private MusicPlayer musicPlayer;
    static LinkedList<Track> recentlyPlayed;

    private Download download;

    private SignInButton SignIn;
    private static final int REQ_CODE = 9001;
    GoogleApiClient googleApiClient;

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
        SignIn = (SignInButton) findViewById(R.id.main_googlesigninbtn);
        SignIn.setOnClickListener(this);
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API,signInOptions).build();


        // Check mode and switch
        switchModes(null);

        // music url
        Uri music_uri = Uri.parse("http://soundbible.com/grab.php?id=2191&type=zip");//Uri.parse("https://www.androidtutorialpoint.com/wp-content/uploads/2016/09/AndroidDownloadManager.mp3");
        DownloadManager dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        download = new Download(dm, this);
        download.downloadData(music_uri);

        musicPlayer = new MusicPlayer(this, new MediaPlayer());

        //musicPlayer.loadSongs();

        // Initialize the library list
        ListView listView = findViewById(R.id.listView);
        PlayListAdapter adapter = new PlayListAdapter(this, musicPlayer.getTrackList());
        listView.setAdapter(adapter);

        MusicController controller = new MusicController(this, adapter, musicPlayer);
        if (!normalMode) {
            musicPlayer.createFlashback();
        }

        registerReceiver(m_timeChangedReceiver, s_intentFilter);

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
                modeSwitch.setText("V");
                normalMode = false;
            } else if (mode.equals(getResources().getString(R.string.mode_vibe))) {
                editor.putString("mode", getResources().getString(R.string.mode_normal));
                Button modeSwitch = (Button) findViewById(R.id.flashbackButton);
                modeSwitch.setText("N");
                normalMode = true;
            }
        }

        else {
            editor.putString("mode", getResources().getString(R.string.mode_vibe));
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

        } else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 123: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    musicPlayer.loadSongs();
                } else {

                    checkPermission();
                }
                return;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.main_googlesigninbtn:
                signIn();
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void signIn(){
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, REQ_CODE);
    }

    private void handleResult(GoogleSignInResult result)
    {
        if(result.isSuccess())
        {
            GoogleSignInAccount account = result.getSignInAccount();
            String name = account.getDisplayName();
            SignIn.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_CODE)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }
}
