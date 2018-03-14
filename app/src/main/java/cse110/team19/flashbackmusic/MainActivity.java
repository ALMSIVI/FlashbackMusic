package cse110.team19.flashbackmusic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

/**
 * Created by Meeta on 3/6/18.
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {
    //region Variables
    private Download download;
    private MusicController controller;
    private String directory;
    private GPSTracker gpsTracker;
    private Intent locationIntent;
    private BroadcastReceiver broadcastReceiver;

    private PlayListAdapter adapter;
    private MusicPlayer player;

    private SignInButton SignIn;
    private static final int REQ_CODE = 9001;
    GoogleApiClient googleApiClient;
    //endregion

    // UI stuff
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;

    // Monitors time change
    private BroadcastReceiver timeChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_TIME_CHANGED)) {
                // TODO: Update playlist based on time and day
            }
        }
    };

    // Download complete
    private BroadcastReceiver downloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE) ){
                Bundle extras = intent.getExtras();
                long id = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
                String filename = download.getLatestFileName(id);

                if (filename != null) {
                    Log.d("newest name", filename);
                    controller.updatePlayList(filename);
                } else {
                    Log.d("newest name", "null");
                }
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

        setContentView(R.layout.nav_action);
        //setContentView(R.layout.activity_main_activity);

        // UI stuff
        toolbar = (Toolbar) findViewById(R.id.nav_action);
        setSupportActionBar(toolbar);
        setContentView(R.layout.activity_main_activity);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Google Signin Activity
        SignIn = (SignInButton) findViewById(R.id.main_googlesigninbtn);
        SignIn.setOnClickListener(this);
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,signInOptions)
                .build();


        directory = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()
                + getResources().getString(R.string.download_folder);
        Log.d("Download directory", directory);

        // Time change
        IntentFilter s_intentFilter = new IntentFilter();
        s_intentFilter.addAction(Intent.ACTION_TIME_TICK);
        s_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        s_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        registerReceiver(timeChanged, s_intentFilter);

        // initializing location services on start up
        gpsTracker = new GPSTracker(this);
        if (gpsTracker.permissionRequest()) {
            locationIntent = new Intent(getApplicationContext(), GPSTracker.class);
            startService(locationIntent);
        }


        // Initialize the library list
        adapter = new PlayListAdapter(this);

        player = new MusicPlayer(new MediaPlayer());


        // Check mode and switch
        SharedPreferences sharedPreferences = this.getSharedPreferences("mode", MODE_PRIVATE);
        String mode = sharedPreferences.getString("mode", null);
        if (mode == null || mode.equals(getResources().getString(R.string.mode_normal))) {
            setNormal();
        } else {
            setVibe();
        }

        registerReceiver(downloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

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
    @SuppressLint("NewApi")
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

    /**
     * Switch to normal mode.
     */
    private void setNormal() {
        // Initialize playlist
        PlayList playList = new NormalPlayList(this, directory);
        adapter.setPlayList(playList);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        // Set up the MVC controller
        controller = new NormalController(this, adapter, player, playList);

        SharedPreferences sharedPreferences = this.getSharedPreferences("mode", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("mode", getResources().getString(R.string.mode_normal));
        editor.apply();
        // Change the button
        Button modeSwitch = (Button) findViewById(R.id.flashbackButton);
        modeSwitch.setText("V");
        // Change the text
        TextView libraryText = findViewById(R.id.libraryText);
        libraryText.setText(R.string.library);
        // Set up playlist
        controller.setUp();
    }

    /**
     * Switch to Vibe mode.
     */
    private void setVibe() {
        // Initialize playlist
        PlayList playList = new VibePlayList(this, directory);
        adapter.setPlayList(playList);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        // Set up the MVC controller
        controller = new VibeController(this, adapter, player, playList);

        SharedPreferences sharedPreferences = this.getSharedPreferences("mode", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("mode", getResources().getString(R.string.mode_vibe));
        editor.apply();
        // Change the button
        Button modeSwitch = (Button) findViewById(R.id.flashbackButton);
        modeSwitch.setText("N");
        // Change the text
        TextView libraryText = findViewById(R.id.libraryText);
        libraryText.setText(R.string.playlist);
        // Set up playlist
        controller.setUp();
    }
    //endregion

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        //stopService(locationIntent);

        unregisterReceiver(timeChanged);
        unregisterReceiver(downloadComplete);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // navigation view item clicks
        switch (item.getItemId()) {
            case R.id.RecentlyPlayed: {
                if (controller.isNormalMode()) {
                    ((NormalController)controller).updatePlayList(NormalPlayList.Sort.Recent);
                }
                break;
            }
            case R.id.Tracks: {
                if (controller.isNormalMode()) {
                    ((NormalController)controller).updatePlayList(NormalPlayList.Sort.Name);
                }
                break;
            }
            case R.id.Albums: {
                if (controller.isNormalMode()) {
                    ((NormalController)controller).updatePlayList(NormalPlayList.Sort.Album);
                }
                break;
            }
            case R.id.Artists: {
                if (controller.isNormalMode()) {
                    ((NormalController)controller).updatePlayList(NormalPlayList.Sort.Artist);
                }
                break;
            }
            case R.id.Favorites: {
                if (controller.isNormalMode()) {
                    ((NormalController)controller).updatePlayList(NormalPlayList.Sort.Favorite);
                }
                break;
            }
            case R.id.Download: {
                download();
                break;
            }
        }
        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
            Log.d("user name", name);
            SignIn.setVisibility(View.GONE);
        }

        else {
            result.getStatus().getStatusMessage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult", "reached");
        Log.d("requestCode and REQ_CODE", requestCode + " " + REQ_CODE);
        if(requestCode == REQ_CODE)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }

    public void download() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Download song/album");
        alert.setMessage("Enter URL");
        // Set EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                String url = input.getText().toString();
                DownloadManager dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                download = new Download(dm, getResources().getString(R.string.download_folder));
                download.downloadData(url);

                String directory = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()
                        + getResources().getString(R.string.download_folder);
                Log.d("Download directory", directory);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cancelled.
            }
        });

        alert.show();
    }

    //endregion
}
