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
     * @param c FlashbackMode
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

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (isPlaying != null) {
                    location = gpstracker.getLocation();
                    isPlaying.updateInfo(location, time.getTime());

                    saveTrackInfo(true, isPlaying);
                    if (audioResourceId.size() > audioIndex) {
                        loadMedia(audioResourceId.get(audioIndex).first,
                                audioResourceId.get(audioIndex).second);
                    } else {
                        changePausePlay();
                        updateText();
                    }
                }
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });

        // Load the songs to the player
        audioResourceId = new ArrayList<Pair<Integer, Track>>();
        audioIndex = 0;
        changePlayPause();
        for (Track t : playList) {
            Log.d("audioIndex", audioIndex + "");
            int id = t.getResourceId();
            audioResourceId.add(new Pair<Integer, Track>(id, t));
            Log.d("trackname", t.getTrackName());
            Log.d("track number", t.getTrackNumber() + "");
            AssetFileDescriptor assetFileDescriptor = context.getResources().openRawResourceFd(id);
            try {
                mediaPlayer.setDataSource(assetFileDescriptor);
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }

        loadMedia(audioResourceId.get(audioIndex).first, audioResourceId.get(audioIndex).second);
        mediaPlayer.start();
    }

    public Track getIsPlaying() {
        return isPlaying;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final Track track = (Track) getItem(i);

        // inflate the view
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.playlist_view, null);
        }

        TextView track_name = (TextView) view.findViewById(R.id.track_name);
        track_name.setText(track.getTrackName());


        final Button status_button = (Button) view.findViewById(R.id.set_status);
        changeButton(track, status_button);

        status_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                track.updateStatus();
                saveTrackInfo(false, track);
                changeButton(track, status_button);

                if (track.getStatus() == -1 && mediaPlayer.isPlaying() && isPlaying == track) {
                    mediaPlayer.stop();
                    if (audioResourceId.size() > audioIndex) {
                        loadMedia(audioResourceId.get(audioIndex).first, audioResourceId.get(audioIndex).second);
                    }
                }
            }
        });
        return view;
    }

    public void loadMedia(int resourceId, Track track) {
        isPlaying = track;
        changePlayPause();
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

    private void changePlayPause() {
        Button mainPlayButton = (Button) ((Activity) context).findViewById(R.id.playButton);
        Drawable pause = context.getResources().getDrawable(R.drawable.ic_pause_actuallyblack_24dp);
        mainPlayButton.setCompoundDrawablesWithIntrinsicBounds(null, pause, null, null);
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
     * Stores the current song's status into sharedPreferences. This method does NOT update the info.
     */
    private void saveTrackInfo(boolean all, Track track) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("track_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(track.getTrackName() + "Status", track.getStatus());

        if (all) {
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
