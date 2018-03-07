package cse110.team19.flashbackmusic;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Tyler on 3/6/18.
 * Source: https://developer.android.com/guide/topics/ui/layout/recyclerview.html#customizing
 * Source: https://www.androidhive.info/2016/01/android-working-with-recycler-view/
 */

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    // data source
    private PlayList playList;

    // constructor
    public Adapter(PlayList playList) {
        this.playList = playList;
    }

    // class to define content within each view
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView trackName, artistName, albumName;
        public Button status;

        public ViewHolder(View view) {
            super(view);
            this.trackName = (TextView) view.findViewById(R.id.trackName);
            this.artistName = (TextView) view.findViewById(R.id.artistName);
            this.albumName = (TextView) view.findViewById(R.id.albumName);
            this.status = (Button) view.findViewById(R.id.status);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_recycler_view, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        List<Track> trackList = playList.getPlayList();
        holder.trackName.setText(trackList.get(position).getTrackName());
        holder.artistName.setText(trackList.get(position).getArtistName());
        holder.albumName.setText(trackList.get(position).getAlbumName());
    }

    @Override
    public int getItemCount() {
        return playList.getCount();
    }
}
