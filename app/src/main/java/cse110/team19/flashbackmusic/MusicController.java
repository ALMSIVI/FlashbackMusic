package cse110.team19.flashbackmusic;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by YueWu on 3/9/2018.
 * Controller of MVC.
 * Model: MainActivity and MusicPlayer
 * View: List of tracks/PlayListAdapter
 */

public class MusicController {
    //region Variables
    private MainActivity mainActivity;
    private MusicPlayer player;

    private BaseAdapter adapter;
    private PlayList playList;

    private Track isPlaying;

    // for recording location of song
    private Date time;
    private Geocoder geocoder;
    private List<Address> addresses;
    private GPSTracker gpstracker;
    private Location location;
    //endregion

    //region Constructor
    /**
     * Constructor.
     * @param mainActivity
     * @param adapter
     * @param player
     */
    public MusicController(MainActivity mainActivity, PlayListAdapter adapter, MusicPlayer player,
                           PlayList playList) {
        this.mainActivity = mainActivity;
        this.adapter = adapter;
        adapter.setController(this);
        this.player = player;
        player.setController(this);
        this.playList = playList;
        // Initialize the locations
        geocoder = new Geocoder(mainActivity, Locale.getDefault());
        gpstracker = new GPSTracker(mainActivity);
        //location = gpstracker.getLocation();
        time = new Date();
    }
    //endregion

    //region Buttons
    public void changePlayPauseButton() {
        if (player.isPlaying()) {
            player.pause();
            changePlay();
        } else {
            player.play();
            changePause();
        }
    }

    /**
     * Changes the button to play.
     */
    private void changePlay() {
        Button mainPauseButton = (Button) mainActivity.findViewById(R.id.playButton);
        // Old version: mainActivity.getResources().getDrawable(...);
        Drawable play = ContextCompat.getDrawable(mainActivity, R.drawable.ic_play_arrow_actuallyblack_24dp);
        mainPauseButton.setCompoundDrawablesWithIntrinsicBounds(null, play, null, null);
    }

    /**
     * Changes the button to pause.
     */
    private void changePause() {
        Button mainPlayButton = (Button) mainActivity.findViewById(R.id.playButton);
        Drawable pause = ContextCompat.getDrawable(mainActivity, R.drawable.ic_pause_actuallyblack_24dp);
        mainPlayButton.setCompoundDrawablesWithIntrinsicBounds(null, pause, null, null);
    }

    public void changeStatusButton(Track track, Button button) {
        int stat = track.getStatus();
        if (stat == 0) {
            Drawable neutral = ContextCompat.getDrawable(mainActivity, R.drawable.neutral);
            button.setCompoundDrawablesWithIntrinsicBounds(null, neutral, null, null);
        } else if (stat == 1) {
            Drawable liked = ContextCompat.getDrawable(mainActivity, R.drawable.favorite);
            button.setCompoundDrawablesWithIntrinsicBounds(null, liked, null, null);
        } else if (stat == -1) {
            Drawable disliked = ContextCompat.getDrawable(mainActivity, R.drawable.dislike);
            button.setCompoundDrawablesWithIntrinsicBounds(null, disliked, null, null);
        }
    }
    //endregion

    //region Track
    public Track getIsPlaying() {
        return isPlaying;
    }

    public void changeStatus(Track track, Button button) {
        track.updateStatus();
        saveTrackInfo(false, track);
        changeStatusButton(track, button);

        if (track.getStatus() == -1 && player.isPlaying() && isPlaying == track) {
            player.stop();
            // TODO: fix this
            //if (audioResourceId.size() > audioIndex) {
            //    loadMedia(audioResourceId.get(audioIndex).first, audioResourceId.get(audioIndex).second);
            //}
        }
    }

    public void updateTrackInfo() {
        //location = gpstracker.getLocation();
        isPlaying.updateInfo(location, time.getTime());
    }

    /**
     * Stores the current song's status into sharedPreferences. This method does NOT update the info.
     */
    public void saveTrackInfo(boolean all, Track track) {
        // TODO: Change to firebase
        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences("track_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(track.getTrackName() + "Status", track.getStatus());

        if (all) {
            editor.putString(track.getTrackName() + "Time", track.getTime() != null ?
                    track.getTime().toString() :
                    "null");
            editor.putString(track.getTrackName() + "Location", track.getLocation());
        }
        editor.apply();
    }

    public void updatePlayList(String filename) {
        playList.addTrack(filename);
        playList.sort();
        adapter.notifyDataSetChanged();
    }
    //endregion

    //region MediaPlayer
    public void playSong(Track track) {
        if (track.getStatus() > -1) {
            changePause();

            //audioResourceId = new <Pair<Integer, Track>>ArrayList();
            //audioResourceId.add(new Pair<Integer, Track>(id, track));
            loadMedia(track);
            isPlaying = track;
        }
    }

    public void loadMedia(Track track) {
        player.stop();
        isPlaying = track;
        player.resetMusic();
        try {
            player.setDataSource(track);
            player.prepareAsync();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        updateText();
    }

    public Track getNext() {
        return null;
    }

    public void resetMusic() {
        player.resetMusic();
    }
    //endregion

    //region Texts
    public void updateText() {
        // Update the "Now playing" text
        TextView infoView = mainActivity.findViewById(R.id.info);
        infoView.setText(isPlaying.getTrackName());
        // Update the "Last played" text
        TextView lastPlayedView = mainActivity.findViewById(R.id.lastPlayed);
        if (isPlaying.getTime() == null) {
            lastPlayedView.setText(mainActivity.getString(R.string.never_played_info));
        } else {
            String lastLocation = "Unknown location";

            if (location != null) {
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                lastLocation = addresses.get(0).getFeatureName();
            }

            String lastPlayedInfo = String.format(
                    mainActivity.getString(R.string.last_played_info),
                    isPlaying.getTime(), lastLocation);
            lastPlayedView.setText(lastPlayedInfo);
        }
    }
    //endregion

    //region Modes
    public boolean isNormalMode() {
        return playList.isNormalMode();
    }

    public void setUpVibe() {
        player.stop();
        playList.createVibePlayList();
        adapter.notifyDataSetChanged();
        player.play();
    }

    public void setUpNormal() {
        playList.createNormalPlayList();
        adapter.notifyDataSetChanged();
    }
    //endregion
}
