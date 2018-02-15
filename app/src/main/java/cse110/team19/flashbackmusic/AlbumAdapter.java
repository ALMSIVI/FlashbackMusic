package cse110.team19.flashbackmusic;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YueWu on 2/12/2018.
 * AlbumAdapter shows a list of albums, but not tracks.
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    private RecyclerView tracks;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView album;
        private TextView artist;
        private Context context;
        // linear layout for tracks
        private LinearLayout linearLayout_tracks;

        public ViewHolder(View view) {
            super(view);
            album = view.findViewById(R.id.albumTitle);
            artist = view.findViewById(R.id.artist);
            context = view.getContext();
            //linearLayout_tracks = view.findViewById(R.id.tracks);
            linearLayout_tracks.setVisibility(View.GONE);
            int intMaxNoOfTracks = 0;
            // finding max number of tracks in an album
            for (int index = 0; index < albums.size(); index++) {
                int intMaxSizeTemp = albums.get(index).getTracks().size();
                if (intMaxSizeTemp > intMaxNoOfTracks) intMaxNoOfTracks = intMaxSizeTemp;
            }
            // adds textview for max number of tracks
            for (int indexView = 0; indexView < intMaxNoOfTracks; indexView++) {
                TextView textView = new TextView(context);
                textView.setId(indexView);
                textView.setPadding(0, 20, 0, 20);
                //textView.setBackground(ContextCompat.getDrawable(context, R.drawable.background_sub_module_text));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                textView.setOnClickListener(this);
                linearLayout_tracks.addView(textView, layoutParams);
            }
            // sets on click listener for album text
            album.setOnClickListener(this);
        }

        @Override
        // shows tracks when album name is clicked
        public void onClick(View view) {
            if (view.getId() == R.id.albumTitle) {
                if (linearLayout_tracks.getVisibility() == View.VISIBLE) {
                    linearLayout_tracks.setVisibility(View.GONE);
                } else {
                    linearLayout_tracks.setVisibility(View.VISIBLE);
                }
            } else {
                //TextView textViewClicked = (TextView) view;
                //Toast.makeText(context, "" + textViewClicked.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private List<Album> albums;

    /**
     * Constructor.
     * @param albumList list of albums to be shown
     */
    public AlbumAdapter(List<Album> albumList) {
        albums = albumList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.albumholder, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // sets album and artist names
        Album albumName = albums.get(position);
        holder.album.setText(albumName.getTitle());
        holder.artist.setText(albumName.getArtist());

        int noOfTrackTextViews = holder.linearLayout_tracks.getChildCount();
        int noOfTrack = albumName.getTracks().size();
        // removes excess textviews created
        if (noOfTrack < noOfTrackTextViews) {
            for (int index = noOfTrack; index < noOfTrackTextViews; index++) {
                TextView currentTextView = (TextView) holder.linearLayout_tracks.getChildAt(index);
                currentTextView.setVisibility(View.GONE);
            }
        }
        // sets on click listener for each track
        for (int textViewIndex = 0; textViewIndex < noOfTrack; textViewIndex++) {
            TextView currentTextView = (TextView) holder.linearLayout_tracks.getChildAt(textViewIndex);
            currentTextView.setText(albumName.getTracks().get(textViewIndex).getTrackName());
                //TODO: ADD CODE HERE TO PLAY TRACK
                /*currentTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(mContext, "" + ((TextView) view).getText().toString(), Toast.LENGTH_SHORT).show();
                    }
                });*/
        }
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }
}
