package cse110.team19.flashbackmusic;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by YueWu on 3/9/2018.
 * Controller of MVC.
 * Model: MainActivity and MusicPlayer
 * View: List of tracks/PlayListAdapter
 */

public abstract class MusicController {
    //region Variables
    protected MainActivity mainActivity;
    protected MusicPlayer player;

    protected BaseAdapter adapter;
    protected PlayList playList;

    protected int isPlaying;

    // for recording information of song
    protected Geocoder geocoder;
    protected List<Address> addresses;
    protected GPSTracker gpstracker;
    protected Location location;
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
    public void changePlay() {
        Button mainPauseButton = (Button) mainActivity.findViewById(R.id.playButton);
        // Old version: mainActivity.getResources().getDrawable(...);
        Drawable play = ContextCompat.getDrawable(mainActivity, R.drawable.ic_play_arrow_actuallyblack_24dp);
        mainPauseButton.setCompoundDrawablesWithIntrinsicBounds(null, play, null, null);
    }

    /**
     * Changes the button to pause.
     */
    public void changePause() {
        Button mainPlayButton = (Button) mainActivity.findViewById(R.id.playButton);
        Drawable pause = ContextCompat.getDrawable(mainActivity, R.drawable.ic_pause_actuallyblack_24dp);
        mainPlayButton.setCompoundDrawablesWithIntrinsicBounds(null, pause, null, null);
    }

    public void changeStatusButton(int id, Button button) {
        int stat = playList.get(id).getStatus();
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
        return playList.get(isPlaying);
    }

    public void changeStatus(int id, Button button) {
        Track track = playList.get(id);
        track.updateStatus();
        saveTrackInfo(false, track);
        changeStatusButton(id, button);

        if (track.getStatus() == -1 && player.isPlaying() && isPlaying == id) {
            player.playNext();
        }
    }

    public void updateTrackInfo() {
        location = gpstracker.getLocation();
        //location = gpstracker.getLocation();
        getIsPlaying().updateInfo(location, MockTime.now());
    }

    /**
     * Stores the current song's status into sharedPreferences. This method does NOT update the info.
     */
    public void saveTrackInfo(boolean all, Track track) {
        // Put status into shared preferences
        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences("tracks", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(track.getPathName() + "Status", track.getStatus());
        editor.apply();

        if (all) {

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference("track");

            Log.d("firebase now website", track.getWebsite());

            reference = reference.child(track.getTrackName());

            Map<String, Object> trackInfo = new HashMap<String, Object>();
            trackInfo.put("trackName", getIsPlaying().getTrackName());
            trackInfo.put("website", getIsPlaying().getWebsite());

            LocalDateTime dateTime = getIsPlaying().getDate();
            trackInfo.put("year", dateTime.getYear());
            trackInfo.put("month", dateTime.getMonthValue());
            trackInfo.put("day", dateTime.getDayOfMonth());
            trackInfo.put("hour", dateTime.getHour());
            trackInfo.put("minute", dateTime.getMinute());

            // TODO: put person
            reference.updateChildren(trackInfo);

        }
    }

    public void updatePlayList(String filename) {
        playList.addTrack(filename);
        playList.sort();
        adapter.notifyDataSetChanged();
    }
    //endregion

    //region MediaPlayer
    public void playSong(int id) {
        if (playList.get(id).getStatus() > -1) {
            changePause();
            isPlaying = id;

            player.reset();

            Track track = getIsPlaying();
            Log.d("Current song track", track.getTrackName());
            Log.d("Current song path", track.getPathName());

            player.setDataSource(getIsPlaying());
            player.prepareAsync();

            updateText();
        }
    }

    public Track getNext() {
        if (isPlaying == playList.size() - 1) {
            return null;
        } else {
            isPlaying++;
            Log.d("isPlaying", Integer.toString(isPlaying));
            return getIsPlaying();
        }
    }

    public void playNext() {
        if (getNext() != null) {
            playSong(isPlaying);
        }
    }

    public void resetMusic() {
        player.resetMusic();
    }
    //endregion

    //region Texts
    public void updateText() {
        // Update the "Now playing" text
        TextView infoView = mainActivity.findViewById(R.id.trackInfo);
        infoView.setText(getIsPlaying().getTrackName());
        // Update the "Last played" text
        TextView lastPlayedView = mainActivity.findViewById(R.id.timeInfo);
        if (getIsPlaying().getDate() == null) {
            lastPlayedView.setText(mainActivity.getString(R.string.never_played_info));
        } else {
            String lastLocation = "Unknown location";

            if (location != null) {
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                lastLocation = addresses.get(0).getAddressLine(0);
            }

            LocalDateTime date = getIsPlaying().getDate();
            String lastPlayedInfo = String.format(
                    mainActivity.getString(R.string.time_info), date.getMonthValue(),
                    date.getDayOfMonth(), date.getYear(), date.getHour(), date.getMinute());
            lastPlayedView.setText(lastPlayedInfo);
        }
    }
    //endregion

    //region Modes
    public abstract boolean isNormalMode();

    public abstract void setUp();

    public abstract void sortPlayList(PlayList.Sort sort);
    //endregion
}
