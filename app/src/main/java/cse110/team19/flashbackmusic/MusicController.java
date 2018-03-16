package cse110.team19.flashbackmusic;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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

    protected List<User> users;

    protected int isPlaying = -1;

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
        } else if (isPlaying != -1){
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
        Drawable play = ContextCompat.getDrawable(mainActivity, R.mipmap.playwhite);
        mainPauseButton.setCompoundDrawablesWithIntrinsicBounds(null, play, null, null);
    }

    /**
     * Changes the button to pause.
     */
    public void changePause() {
        Button mainPlayButton = (Button) mainActivity.findViewById(R.id.playButton);
        Drawable pause = ContextCompat.getDrawable(mainActivity, R.mipmap.pausewhite);
        mainPlayButton.setCompoundDrawablesWithIntrinsicBounds(null, pause, null, null);
    }

    public void changeStatusButton(int id, Button button) {
        int stat = playList.get(id).getStatus();
        if (stat == 0) {
            Drawable neutral = ContextCompat.getDrawable(mainActivity, R.mipmap.neutral);
            button.setCompoundDrawablesWithIntrinsicBounds(null, neutral, null, null);
        } else if (stat == 1) {
            Drawable liked = ContextCompat.getDrawable(mainActivity, R.mipmap.like);
            button.setCompoundDrawablesWithIntrinsicBounds(null, liked, null, null);
        } else if (stat == -1) {
            Drawable disliked = ContextCompat.getDrawable(mainActivity, R.mipmap.dislike);
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

           TrackDataHandler handler = new TrackDataHandler(mainActivity);
           handler.writeTrack(track);

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
        TrackDataHandler handler = new TrackDataHandler(mainActivity);
        handler.retrieveTrackNecessary(getIsPlaying());

        // trackInfo
        TextView trackInfo = mainActivity.findViewById(R.id.trackInfo);
        trackInfo.setText(getIsPlaying().getTrackName());

        // albumInfo
        TextView albumInfo = mainActivity.findViewById(R.id.albumInfo);
        albumInfo.setText(getIsPlaying().getAlbumName());


        TextView timeInfo = mainActivity.findViewById(R.id.timeInfo);
        if (getIsPlaying().getDate() == null) {
            timeInfo.setText(mainActivity.getString(R.string.never_played_info));
        } else {
            // locationInfo
            String lastLocation = "Unknown location";

            if (location != null) {
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                lastLocation = addresses.get(0).getAddressLine(0);

                String locationString = String.format(mainActivity.getString(R.string.location_info), lastLocation);
                TextView locationInfo = mainActivity.findViewById(R.id.locationInfo);
                locationInfo.setText(locationString);
            }

            // userInfo
            TextView userInfo = mainActivity.findViewById(R.id.userInfo);
            if (users == null) {
                userInfo.setText(mainActivity.getString(R.string.default_info));
            } else {
                boolean updated = false;
                for (User user : users) {
                    if (user.getId().equals(getIsPlaying().getPersonLastPlayed())) {
                        // Equal to current user
                        /*if (user.getId().equals(mainActivity.getCurrentUser().getId())) {
                            String userString = String.format(mainActivity.getString(R.string.user_info), "upi");
                            SpannableStringBuilder str = new SpannableStringBuilder(userString);
                            // TODO: debug
                            str.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), 21, 24, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            userInfo.setText(str);
                        } else {

                        }*/
                        String userString = String.format(mainActivity.getString(R.string.user_info), user.getName());
                        userInfo.setText(userString);
                        break;
                    }
                }

                String userString = String.format(mainActivity.getString(R.string.user_info), "Anonymous " + getIsPlaying().getPersonLastPlayed());
            }

            // timeInfo
            LocalDateTime date = getIsPlaying().getDate();
            String lastPlayedInfo = String.format(
                    mainActivity.getString(R.string.time_info), date.getMonthValue(),
                    date.getDayOfMonth(), date.getYear(), String.format("%02d",date.getHour()),
                    String.format("%02d", date.getMinute()));
            timeInfo.setText(lastPlayedInfo);
        }
    }

    public void clearText() {
        TextView trackInfo = mainActivity.findViewById(R.id.trackInfo);
        trackInfo.setText("");

        TextView albumInfo = mainActivity.findViewById(R.id.albumInfo);
        albumInfo.setText("");

        TextView userInfo = mainActivity.findViewById(R.id.userInfo);
        userInfo.setText("");

        TextView locationInfo = mainActivity.findViewById(R.id.locationInfo);
        locationInfo.setText("");

        TextView timeInfo = mainActivity.findViewById(R.id.userInfo);
        userInfo.setText("");
    }
    //endregion

    //region Modes
    public abstract boolean isNormalMode();

    public abstract void setUp();

    public abstract void sortPlayList(PlayList.Sort sort);
    //endregion
}
