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
    // MVC
    private MusicController controller;

    private Context context;
    private List<Track> playList;

    /**
     * Constructor.
     *
     * @param l playlist
     */
    public PlayListAdapter(Context context, List<Track> l) {
        this.context = context;
        playList = l;
    }


    public void setController(MusicController controller) {
        this.controller = controller;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final Track track = (Track) getItem(i);

        // inflate the view
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.playlist_view, null);
        }

        TextView newTrack = (TextView) view.findViewById(R.id.track_name);
        newTrack.setText(track.getTrackName());
        newTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.playSong(track);
            }
        });

        final Button statusButton = (Button) view.findViewById(R.id.set_status);
        controller.changeStatusButton(track, statusButton);

        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.changeStatus(track, statusButton);
            }
        });
        return view;
    }


    //region Unimportant Overridden Methods
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
    //endregion
}