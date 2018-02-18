package cse110.team19.flashbackmusic;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
    private ArrayList<Integer> audioResourceId;
    private int audioIndex = 0;
    private Track isPlaying;

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

        mediaPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        isPlaying.updateStatus();
                        if (audioResourceId.size() > audioIndex) {
                            loadMedia(audioResourceId.get(audioIndex));
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
        final Track track = null;
        // inflate the view
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.child_view, null);
        }

        TextView track_name = (TextView) view.findViewById(R.id.track_name);
        track_name.setText(track.getTrackName());
        track_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePlayPause(view);
                int id = track.getResourceId();
                loadMedia(id);
                isPlaying = track;
            }
        });
        // TODO set TypeFace here, low priority, just to make things pretty

        final Button status_button = (Button) view.findViewById(R.id.set_status);
        final SharedPreferences sharedPreferences = context.getSharedPreferences("user_name", MODE_PRIVATE);
        changeButton(track, status_button);

        status_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                track.updateStatus();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putStringSet(track.getTrackName(), track.getInfo());
                changeButton(track, status_button);
                //for (Track t : trackArray)
            }
        });
        return view;
    }

    public void changeButton(Track track, Button button) {
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
    /**
     * Load one media file into the player.
     *
     * @param resourceId id of the media file in system.
     */
    public void loadMedia(int resourceId) {
        mediaPlayer.reset();
        AssetFileDescriptor assetFileDescriptor = context.getResources().openRawResourceFd(resourceId);
        try {
            mediaPlayer.setDataSource(assetFileDescriptor);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        audioIndex++;
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
