package cse110.team19.flashbackmusic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by YueWu on 2/12/2018.
 */
public class PlayListAdapter extends BaseAdapter {
    private Context context;
    private MediaPlayer mediaPlayer;
    private List<Track> playList;
    private ArrayList<Pair<Integer, Track>> audioResourceId;
    private int audioIndex = 0;
    private Track isPlaying;
    private Date time;

    // for recording location of song
    Geocoder geocoder;
    List<Address> addresses;
    GPSTracker gpstracker;
    Location location;

    /**
     * Constructor.
     *
     * @param c
     * @param l playlist
     * @param m media player
     */
    public PlayListAdapter(Context c, List<Track> l, MediaPlayer m) {
        context = c;
        playList = l;
        mediaPlayer = m;

        // Initialize the locations
        geocoder = new Geocoder(context, Locale.getDefault());
        gpstracker = new GPSTracker(context);
        location = gpstracker.getLocation();
        time = new Date();

        mediaPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {

                        location = gpstracker.getLocation();
                        isPlaying.updateInfo(location, time.getTime());

                        saveTrackInfo(true, isPlaying);

                        if(audioResourceId.size() > audioIndex) {
                            loadMedia(audioResourceId.get(audioIndex).first, audioResourceId.get(audioIndex).second);
                        }
                        else{
                            changePausePlay();
                            updateText();
                        }
                    }
                }
        );

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                // Update the "Now playing" text
            }
        });
    }

    public Track getIsPlaying() {
        return isPlaying;
    }

    public void changePlayPause(View view) {
        Button mainPlayButton = (Button) ((Activity) context).findViewById(R.id.playButton);
        Drawable pause = context.getResources().getDrawable(R.drawable.ic_pause_actuallyblack_24dp);
        mainPlayButton.setCompoundDrawablesWithIntrinsicBounds(null, pause, null, null);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final Track track = (Track) getItem(i);
        updateTrackInfo(track);

        Log.d("track name flashback", track.getTrackName());
        // inflate the view
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.playlist_view, null);
        }

        TextView track_name = (TextView) view.findViewById(R.id.track_name);
        track_name.setText(track.getTrackName());

        // TODO set TypeFace here, low priority, just to make things pretty

        final Button status_button = (Button) view.findViewById(R.id.set_status);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        changeButton(track, status_button);

        status_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                track.updateStatus();
                saveTrackInfo(false, track);
                changeButton(track, status_button);
                //for (Track t : trackArray)
            }
        });
        return view;
    }

    private void changeButton(Track track, Button button) {
        int stat = track.getStatus();
        if (stat == 0) {
            Drawable neutral = context.getResources().getDrawable(R.drawable.neutral);
            button.setCompoundDrawablesWithIntrinsicBounds(null, neutral, null, null);
        } else if (stat == 1) {
            Drawable liked = context.getResources().getDrawable(R.drawable.favorite);
            button.setCompoundDrawablesWithIntrinsicBounds(null, liked, null, null);
        } else if (stat == -1) {
            Drawable disliked = context.getResources().getDrawable(R.drawable.dislike);
            button.setCompoundDrawablesWithIntrinsicBounds(null, disliked, null, null);
        }
    }

    private void changePausePlay() {
        Button mainPauseButton = (Button) ((Activity) context).findViewById(R.id.playButton);
        Drawable play = context.getResources().getDrawable(R.drawable.ic_play_arrow_actuallyblack_24dp);
        mainPauseButton.setCompoundDrawablesWithIntrinsicBounds(null, play, null, null);
    }

    /**
     * Load one media file into the player.
     *
     * @param resourceId id of the media file in system.
     */
    public void loadMedia(int resourceId, Track t) {
        isPlaying = t;
        mediaPlayer.reset();
        AssetFileDescriptor assetFileDescriptor = context.getResources().openRawResourceFd(resourceId);
        try {
            mediaPlayer.setDataSource(assetFileDescriptor);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        updateText();

        audioIndex++;
    }


    private void updateTrackInfo(Track t) {
        // Retrieve data from sharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        int status = sharedPreferences.getInt(t.getTrackName() + "Status", 0);
        t.setStatus(status);


        // calendar
        String cal = sharedPreferences.getString(t.getTrackName() + "Time", null);
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        if (cal != null) {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(format.parse(cal));
                t.setCalendar(calendar);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // location
        String loc = sharedPreferences.getString(t.getTrackName() + "Location", "Unknown Location");
        if (!loc.equals("Unknown Location")) {
            String[] locationValue = loc.split("");
            double latitude = Double.parseDouble(locationValue[0]);
            double longitude = Double.parseDouble(locationValue[1]);
            Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            t.setLocation(location);
        }
    }

    private void updateText() {
        // Update the "Now playing" text
        TextView infoView = ((Activity) context).findViewById(R.id.info);
        infoView.setText(isPlaying.getTrackName());
        // Update the "Last played" text
        TextView lastPlayedView = ((Activity) context).findViewById(R.id.lastPlayed);
        if (isPlaying.getTime() == null) {
            lastPlayedView.setText(context.getString(R.string.never_played_info));
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
                    context.getString(R.string.last_played_info),
                    isPlaying.getTime(), lastLocation);
            lastPlayedView.setText(lastPlayedInfo);
        }
    }

    /**
     * Store the current song's info into sharedPreferences. This method does NOT update song's info.
     */
    private void saveTrackInfo(boolean all, Track track) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(track.getTrackName() + "Status", track.getStatus());

        if(all) {
            editor.putString(track.getTrackName() + "Time", track.getTime());
            editor.putString(track.getTrackName() + "Location", track.getLocation());
        }
        editor.apply();
    }


    /* Overridden methods */
    @Override
    public int getCount() {
        return playList.size();
    }

    @Override
    public Object getItem(int i) {
        return playList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

}
