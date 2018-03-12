package cse110.team19.flashbackmusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by YueWu on 2/12/2018.
 */
public class AlbumAdapter extends BaseAdapter {
    // MVC
    private MusicController controller;

    private Context context;
    private List<Album> albumList;

    /**
     * Constructor.
     *
     * @param l playlist
     */
    public AlbumAdapter(Context context, List<Album> l) {
        this.context = context;
        albumList = l;
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
        return albumList.size();
    }

    @Override
    public Object getItem(int i) {
        return albumList.get(i);
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