package cse110.team19.flashbackmusic;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Tyler on 2/12/18.
 */

public class TrackAdaptor extends RecyclerView.Adapter<TrackAdaptor.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView trackTitle;
        private Button trackStatus;

        public ViewHolder(View view) {
            super(view);
            trackTitle = view.findViewById(R.id.trackTitle);
            trackStatus = view.findViewById(R.id.trackStatus);
        }
    }

    private List<Track> tracks;

    /**
     * Constructor.
     * @param trackList list of tracks in album to be shown
     */
    public TrackAdaptor(List<Track> trackList) {
        trackList = tracks;
    }

    @Override
    public TrackAdaptor.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trackholder, parent, false);

        return new TrackAdaptor.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TrackAdaptor.ViewHolder holder, int position) {
        Track track = tracks.get(position);
        holder.trackTitle.setText(track.getTrackName());
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }
}
