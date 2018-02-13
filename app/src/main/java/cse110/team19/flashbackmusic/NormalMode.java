package cse110.team19.flashbackmusic;

import android.content.res.*;
import android.media.*;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.*;
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

        // Initialize the media player and load songs
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
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

        loadSongs();

        // Initialize the library list
        libraryList = findViewById(R.id.libraryList);
        libraryList.setHasFixedSize(true);
        libraryLayout = new LinearLayoutManager(this);
        libraryList.setLayoutManager(libraryLayout);
        adapter = new AlbumAdapter(albumtracker);
        libraryList.setAdapter(adapter);
    }

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
     * @param view
     */
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

    /**
     * Load the songs.
     */
    public void loadSongs() {
        final Field[] fields = R.raw.class.getFields(); //Gets the all the files (tracks) in raw folder
        for (int count = 0; count < fields.length; count++) { //Goes through each track
            String name = fields[count].getName();

            //Gets id to play the track (used in LoadMedia())
            int resourceID = getResources().getIdentifier(name, "raw", getPackageName());
            audioResourceId.add(resourceID);

            //File path of track
            String path = "android.resource://" + getPackageName() + "/raw/" + name;

            //Gets the metadata of the track (album, artist, track number in album, track name)
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            Resources res = getResources();
            AssetFileDescriptor afd = res.openRawResourceFd(resourceID);
            mmr.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            String albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String trackNumber = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
            String trackName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

            //Create track based off of metadata
            Track t = new Track(trackName, trackNumber, artist);
            Log.d("album name", albumName); //prints album name for debugging

            if (!albums.containsKey(albumName)) {
                Album newAlbum = new Album(albumName, artist);
                albums.put(albumName, newAlbum); //albums is a map that maps the album name to its Album object
                albumtracker.add(newAlbum); //albumtracker has a list of all the album names
                newAlbum.tracks.add(t); //adds the track to the album
            }
            else
                albums.get(albumName).tracks.add(t); //adds the track to the album

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