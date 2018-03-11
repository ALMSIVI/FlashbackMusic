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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.os.Environment;
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

import static android.os.Environment.DIRECTORY_DOWNLOADS;

/**
 * Created by Meeta on 3/6/18.
 */


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{
    private Download download;
    private MusicController controller;
    //endregion

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

        // Google Signin Activity
        SignIn = (SignInButton) findViewById(R.id.main_googlesigninbtn);
        SignIn.setOnClickListener(this);
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API,signInOptions).build();


        // TODO The below code is TESTING purposes only. Remove this when funcionality is complete.
        //Uri music_uri = Uri.parse("http://soundbible.com/grab.php?id=2191&type=zip");
        Uri music_uri = Uri.parse("http://soundbible.com/grab.php?id=2191&type=mp3");
        DownloadManager dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        download = new Download(dm, getResources().getString(R.string.download_folder));
        download.downloadData(music_uri);

        String directory = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()
                + getResources().getString(R.string.download_folder);
        Log.d("Download directory", directory);

        // Initialize playlist
        PlayList playList = new PlayList(this, directory);

        // Initialize the library list
        ListView listView = findViewById(R.id.listView);
        PlayListAdapter adapter = new PlayListAdapter(this, playList);
        listView.setAdapter(adapter);

        // Set up the MVC controller
        controller = new MusicController(this, adapter,
                new MusicPlayer(new MediaPlayer()), playList);

        registerReceiver(m_timeChangedReceiver, s_intentFilter);

        // Check mode and switch
        SharedPreferences sharedPreferences = this.getSharedPreferences("mode", MODE_PRIVATE);
        String mode = sharedPreferences.getString("mode", null);
        if (mode == null || mode.equals(getResources().getString(R.string.mode_normal))) {
            setNormal();
        } else {
            setVibe();
        }
    }


    //region Click Listeners
    /**
     * Click listener for the play button at the bottom of the activity.
     * @param view
     */
    public void playMusic(View view) {
        controller.changePlayPauseButton();
    }

    /**
     * Click listener fo the reset button at the bottom of the activity.
     * @param view
     */
    public void resetMusic(View view) {
        controller.resetMusic();
    }


    /**
     * Switch modes (Normal to Vibe or Vibe to Normal)
     * @param view
     */
    public void switchModes(View view) {
        //Get mode
        SharedPreferences sharedPreferences = this.getSharedPreferences("mode", MODE_PRIVATE);
        String mode = sharedPreferences.getString("mode", null);

        if (mode.equals(getResources().getString(R.string.mode_normal))) {
            setVibe();
        } else { // vibe mode, switch to normal
            setNormal();
        }
    }

    private void setNormal() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("mode", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("mode", getResources().getString(R.string.mode_normal));
        editor.apply();
        Button modeSwitch = (Button) findViewById(R.id.flashbackButton);
        modeSwitch.setText("V");
        controller.setUpNormal();
    }

    private void setVibe() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("mode", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("mode", getResources().getString(R.string.mode_vibe));
        editor.apply();
        Button modeSwitch = (Button) findViewById(R.id.flashbackButton);
        modeSwitch.setText("N");
        controller.setUpVibe();
    }
    //endregion

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(m_timeChangedReceiver);
    }

    //region Permission checking
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
    //endregion

    //region Google Friends
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
    //endregion
}
