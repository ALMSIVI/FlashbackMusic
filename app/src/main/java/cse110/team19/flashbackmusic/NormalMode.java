package cse110.team19.flashbackmusic;

import android.content.res.*;
import android.graphics.drawable.Drawable;
import android.media.*;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

/**
 * This is the normal mode window.
 */
public class NormalMode extends AppCompatActivity {
    /* Members */
    // Player
    private MediaPlayer mediaPlayer;
    ArrayList<Integer> audioResourceId = new ArrayList<Integer>();
    int audioIndex = 0;
    boolean songHasLoaded = false;
    Map<String, Album> albums = new LinkedHashMap<String, Album>();
    List<Album> albumtracker = new ArrayList<Album>();
    // Music Library
    private RecyclerView libraryList;
    private RecyclerView.LayoutManager libraryLayout;
    private AlbumAdapter adapter;

    /* Methods */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_mode);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        loadSongs();

        // Initialize the library list
        libraryList = findViewById(R.id.libraryList);
        libraryList.setHasFixedSize(true);
        libraryLayout = new LinearLayoutManager(this);
        libraryList.setLayoutManager(libraryLayout);
        adapter = new AlbumAdapter(albumtracker);
        libraryList.setAdapter(adapter);



        mediaPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if (audioResourceId.size() > audioIndex) {
                            loadMedia(audioResourceId.get(audioIndex));
                            //mediaPlayer.start();
                            //mediaPlayer.
                        }
                    }
                }
        );

        mediaPlayer.setOnPreparedListener(
                new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                }
        );

    }

    @Override
    public void onStop() {
        super.onStop();
        if (isChangingConfigurations() && mediaPlayer.isPlaying()) {
            ; //"do nothing"
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_normal_mode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void playMusic(View view) {
        Button playButton = findViewById(R.id.playButton);
        //Check if something is already playing
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playButton.setBackgroundResource(android.R.drawable.ic_media_play);
        } else {

            if (songHasLoaded == false) {
                loadMedia(audioResourceId.get(audioIndex));
                songHasLoaded = true;
            }

            //Since there is already a song loaded, just resume the song
            mediaPlayer.start();
            playButton.setBackgroundResource(android.R.drawable.ic_media_pause);
        }
    }

    public void loadSongs() {
        final Field[] fields = R.raw.class.getFields();
        for (int count = 0; count < fields.length; count++) {
            String name = fields[count].getName();
            int resourceID = getResources().getIdentifier(name, "raw", getPackageName());
            String path = "android.resource://" + getPackageName() + "/raw/" + name;

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            Resources res = getResources();
            AssetFileDescriptor afd = res.openRawResourceFd(resourceID);
            mmr.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            //mmr.setDataSource(path);
            String albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String trackNumber = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
            String trackName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            Track t = new Track(trackName);
            t.artist = artist;
            t.trackNumber = trackNumber;
            Log.d("albume name", albumName);

            if (!albums.containsKey(albumName)) {
                Album newAlbum = new Album(albumName);
                albums.put(albumName, newAlbum);
                albumtracker.add(newAlbum);
                newAlbum.tracks.add(t);
            }
            else
                albums.get(albumName).tracks.add(t);

            audioResourceId.add(resourceID);
        }
    }

    public void loadMedia(int resourceId) {
        mediaPlayer.reset();
        AssetFileDescriptor assetFileDescriptor = this.getResources().openRawResourceFd(resourceId);
        try {
            mediaPlayer.setDataSource(assetFileDescriptor);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        audioIndex++;
    }


}