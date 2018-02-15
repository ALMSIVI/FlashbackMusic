package cse110.team19.flashbackmusic;

import android.content.Intent;
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
    private ExpandableListView libraryList;
    private LibraryAdapter adapter;
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
        //TODO: initialize content and the list
        adapter = new LibraryAdapter(this, albumtracker);
        libraryList.setAdapter(adapter);

        libraryList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent,
                                        View view, int i, int i1, long l) {
                TextView trackView = view.findViewById(R.id.track_name);
                String trackName = trackView.getText().toString();
                View parentView = parent.getChildAt(i);
                TextView albumView = parentView.findViewById(R.id.album_name);
                String albumName = albumView.getText().toString();
                int resourceId = albums.get(albumName).getTrack(trackName).getResourceId();
                loadMedia(resourceId);
                mediaPlayer.reset();
                playMusic(view);
            }
        });
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

    public void resetMusic(View view) {
      mediaPlayer.reset();
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

            //Gets the metadata of the track (album, artist, track number in album, track name)
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            Resources res = getResources();
            AssetFileDescriptor afd = res.openRawResourceFd(resourceID);
            mmr.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            String albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String trackNumber = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
            String trackName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

            // Parse the metadata
            if (albumName == null || albumName.equals("")) {
                albumName = "Unknown album";
            }
            if (artist == null || artist.equals("")) {
                artist = "Unknown artist";
            }
            int trackNo = 0;
            int numTracks = 0;
            if (trackNumber != null) {
                String[] numbers = trackNumber.split("/");
                trackNo = Integer.parseInt(numbers[0]);
                numTracks = Integer.parseInt(numbers[1]);
            }
            if (trackName == null || trackName.equals("")) {
                trackName = "Unknown track";
            }

            // Create the track
            Track t = new Track(trackName, trackNo, artist, resourceID);
            Log.d("album name", albumName);
            if (!albums.containsKey(albumName)) {
                Album newAlbum = new Album(albumName, artist, numTracks);
                albums.put(albumName, newAlbum);
                albumtracker.add(newAlbum);
                newAlbum.addTrack(t);
            } else {
                albums.get(albumName).addTrack(t);
            }
        }
    }

    /**
     * Load one media file into the player.
     * @param resourceId id of the media file in system.
     */
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

    /**
     * Switch to Flashback mode.
     * @param view
     */
    public void switchFlashback(View view) {
        Intent intent = new Intent(this, PlayList_Activity.class);
        startActivity(intent);
    }
}