package cse110.team19.flashbackmusic;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.People;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


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


    private String url;


    private PlayListAdapter adapter;
    private MusicPlayer player;

    private SignInButton SignIn;
    private static final int REQ_CODE = 9001;
    GoogleApiClient googleApiClient;

    GoogleSignInAccount signInAccount;
    String serverAuthCode;

    //endregion


    // UI stuff
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;

    // Download complete
    private BroadcastReceiver downloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                Bundle extras = intent.getExtras();
                long id = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
                String filename = download.getLatestFileName(id);

                // Unzip file
                if (filename != null) {
                    SharedPreferences sharedPreferences = getSharedPreferences("tracks", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (filename.contains("zip")) {
                        Log.d("zipFile", filename);
                        File tDirectory = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()
                                + getResources().getString(R.string.download_folder));
                        File zipFile = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()
                                + getResources().getString(R.string.download_album_folder) + filename);
                        ArrayList<String> fileNames = new ArrayList<String>();

                        try {
                            fileNames = download.unzipFile(zipFile, tDirectory);
                        } catch (IOException e) {
                            Log.d("IOException", e.getMessage());
                            System.exit(-1);
                        }

                        for (String file : fileNames) {
                            Log.d("adding new file", file);
                            controller.updatePlayList(file);
                            editor.putString(directory + file + "Website", url);
                        }
                    } else {
                        Log.d("newest name", filename);
                        controller.updatePlayList(filename);
                        editor.putString(directory + filename + "Website", url);
                        Log.d("firebase website", directory + filename);
                    }
                    editor.apply();
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
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                //.enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,signInOptions)
                .build();


        directory = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()
                + getResources().getString(R.string.download_folder);
        Log.d("Download directory", directory);

        // initializing location services on start up
        gpsTracker = new GPSTracker(this);
        if (gpsTracker.permissionRequest()) {
            locationIntent = new Intent(getApplicationContext(), GPSTracker.class);
            startService(locationIntent);
        }

        // Mock time setup
        MockTime.useSystemDefaultZoneClock();

        // Initialize the library list
        adapter = new PlayListAdapter(this);

        player = new MusicPlayer(new MediaPlayer());

        // Check mode and switch
        SharedPreferences sharedPreferences = getSharedPreferences("mode", MODE_PRIVATE);
        String mode = sharedPreferences.getString("mode", null);
        if (mode == null || mode.equals(getResources().getString(R.string.mode_normal))) {
            setNormal();
        } else {
            setVibe();
        }

        registerReceiver(downloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    //region UI listeners
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

    public void skipMusic(View view) {
        if (controller.getNext() != null) {
            controller.playNext();
        }
    }

    /**
     * Switch modes (Normal to Vibe or Vibe to Normal)
     * @param view
     */
    public void switchModes(View view) {
        //Get mode
        SharedPreferences sharedPreferences = getSharedPreferences("mode", MODE_PRIVATE);
        String mode = sharedPreferences.getString("mode", null);

        controller.clearText();

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

        SharedPreferences sharedPreferences = getSharedPreferences("mode", MODE_PRIVATE);
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

        SharedPreferences sharedPreferences = getSharedPreferences("mode", MODE_PRIVATE);
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
                    controller.sortPlayList(NormalPlayList.Sort.Recent);
                }
                break;
            }
            case R.id.Tracks: {
                if (controller.isNormalMode()) {
                    controller.sortPlayList(NormalPlayList.Sort.Name);
                }
                break;
            }
            case R.id.Albums: {
                if (controller.isNormalMode()) {
                    controller.sortPlayList(NormalPlayList.Sort.Album);
                }
                break;
            }
            case R.id.Artists: {
                if (controller.isNormalMode()) {
                    controller.sortPlayList(NormalPlayList.Sort.Artist);
                }
                break;
            }
            case R.id.Favorites: {
                if (controller.isNormalMode()) {
                    controller.sortPlayList(NormalPlayList.Sort.Favorite);
                }
                break;
            }
            case R.id.Download: {
                download();
                break;
            }
            case R.id.Time: {
                mockTime();
            }
        }
        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    //endregion

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
    protected void onDestroy() {
        super.onDestroy();

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        //stopService(locationIntent);

        unregisterReceiver(downloadComplete);
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
            //Log.d("result error message", result.getStatus().getStatusMessage());
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

            if(result.isSuccess())
            {
                signInAccount = result.getSignInAccount();
                serverAuthCode = signInAccount.getServerAuthCode();
                SignIn.setVisibility(View.GONE);
            }
        }
    }

    public static People setUp(Context context, String serverAuthCode) throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();

        String redirectUrl = "";

        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                httpTransport, jacksonFactory, context.getString(R.string.server_client_id),
                context.getString(R.string.server_client_secret), serverAuthCode, redirectUrl)
                .execute();

        GoogleCredential credential = new GoogleCredential.Builder().setClientSecrets(context.getString(R.string.server_client_id)
        , context.getString(R.string.server_client_secret)).setTransport(httpTransport).setJsonFactory(jacksonFactory).build();


        credential.setFromTokenResponse(tokenResponse);

        return new People.Builder(httpTransport, jacksonFactory, credential).setApplicationName("Vibe Music").build();

    }

    //People peopleService = setUp(MainActivity.this, serverAuthCode);

    @Override
    public void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }
    //endregion

    //region Download and Time
    public void download() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Download song/album");
        alert.setMessage("Enter URL");
        // Set EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Download Song", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                url = input.getText().toString();
                Log.d("firebase url", url);
                DownloadManager dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                download = new Download(dm, getResources().getString(R.string.download_folder));
                download.downloadData(url);
            }
        });

        alert.setNegativeButton("Download Album", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                url = input.getText().toString();
                DownloadManager dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                download = new Download(dm, getResources().getString(R.string.download_album_folder));
                download.downloadData(url);
            }
        });

        alert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cancelled.
            }
        });

        alert.show();
    }


    public void mockTime() {
        DatePickerDialog datePicker = new DatePickerDialog(this);

        final DateWrapper wrapper = new DateWrapper();

        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                wrapper.setTime(i, i1);
                MockTime.useFixedClockAt(wrapper.generateDate());
            }
        };

        final TimePickerDialog timePicker = new TimePickerDialog(this, listener, 0, 0, false);
        datePicker.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                wrapper.setDate(i, i1, i2);
                timePicker.show();
            }
        });

        datePicker.show();
    }
    //endregion

}
