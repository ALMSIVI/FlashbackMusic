package cse110.team19.flashbackmusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by YueWu on 2/12/2018.
 */
public class AlbumTrackAdapter extends BaseAdapter {
    // MVC
    private MusicController controller;

    private Context context;
    private Album album;

    /**
     * Constructor.
     *
     * @param a album
     */
    public AlbumTrackAdapter(Context context, Album a) {
        this.context = context;
        album = a;
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
        return album.getNumSongs();
    }

    @Override
    public Object getItem(int i) {
        return album.getTrack(i);
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