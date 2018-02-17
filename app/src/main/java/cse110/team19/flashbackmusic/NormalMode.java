package cse110.team19.flashbackmusic;

import android.content.Context;
import android.content.Intent;
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
import java.lang.reflect.Field;
import java.util.*;

/**
 * This is the normal mode window.
 */
public class NormalMode extends AppCompatActivity {
    /* Members */
    // player
    private MediaPlayer mediaPlayer;
    ArrayList<Integer> audioResourceId = new ArrayList<Integer>();
    int audioIndex = 0;
    boolean songHasLoaded = false;
    boolean listExpanded;
    static LinkedList<Track> recentlyPlayed;

    // for extracting metadata
    Map<String, Album> album_data = new LinkedHashMap<String, Album>();

    // for the LibraryAdaptor
    List<Album> album_list = new ArrayList<Album>();
    Map<Album, List<Track>> album_to_tracks = new LinkedHashMap<Album, List<Track>>();

    // for the expandable list view (the music library
    private ExpandableListView expandableListView;
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
        //TODO: initialize content and the list
        adapter = new LibraryAdapter(this, album_list, album_to_tracks, mediaPlayer);
        expandableListView = findViewById(R.id.expandableListView);
        expandableListView.setAdapter(adapter);

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                if (expandableListView.isGroupExpanded(i)) {
                    expandableListView.collapseGroup(i);
                    return false;
                } else {
                    expandableListView.expandGroup(i);
                    return true;
                }
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent,
                                        View view, int i, int i1, long l) {
                TextView trackView = view.findViewById(R.id.track_name);
                String trackName = trackView.getText().toString();
                View parentView = parent.getChildAt(i);
                TextView albumView = parentView.findViewById(R.id.album_name);
                String albumName = albumView.getText().toString();
                int resourceId = album_data.get(albumName).getTrack(trackName).getResourceId();
                loadMedia(resourceId);
                mediaPlayer.reset();
                playMusic(view);
                // TODO
                return true;
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
        Button playButton = (Button) findViewById(R.id.playButton);
        //Check if something is already playing
        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Drawable pause = getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp);
            playButton.setCompoundDrawablesWithIntrinsicBounds(null, pause, null, null);
        }
        else {
            //Since there is already a song loaded, just resume the song
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }

            Drawable pause = getResources().getDrawable(R.drawable.ic_pause_black_24dp);
            playButton.setCompoundDrawablesWithIntrinsicBounds(null, pause, null, null);
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
            if (!album_data.containsKey(albumName)) {
                Album newAlbum = new Album(albumName, artist, numTracks);
                album_data.put(albumName, newAlbum);
                album_list.add(newAlbum);
                newAlbum.addTrack(t);

                // update data to be sent to adaptor
                List<Track> tracks = new LinkedList<Track>();
                tracks.add(t);
                album_to_tracks.put(newAlbum, tracks);

            } else {
                album_data.get(albumName).addTrack(t);
                album_to_tracks.get(album_data.get(albumName)).add(t);
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
     * @param
     */
    public void switchFlashback() {
        Intent intent = new Intent(this, PlayList_Activity.class);
        startActivity(intent);
    }
}

