package cse110.team19.flashbackmusic;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by YueWu on 2/12/2018.
 * AlbumAdapter shows a list of albums, but not tracks.
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView album;

        public ViewHolder(View view) {
            super(view);
            album = view.findViewById(R.id.albumTitle);
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
        Album albumName = albums.get(position);
        holder.album.setText(albumName.title);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }
}
