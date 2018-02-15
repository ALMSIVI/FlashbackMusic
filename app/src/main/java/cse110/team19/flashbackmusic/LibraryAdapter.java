package cse110.team19.flashbackmusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

/**
 * Created by Tyler on 2/14/18.
 */

public class LibraryAdapter extends BaseExpandableListAdapter {

    private Context context;
    // data source for the albums
    private List<Album> albumData;
    // data source for the tracks within each album
    private Map<Album, List<Track>> trackData;

    // constructor
    public LibraryAdapter(Context c, List<Album> l, Map<Album, List<Track>> h) {
        context = c;
        albumData = l;
        trackData = h;
    }

    @Override
    public int getGroupCount() {
        return albumData.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return trackData.get(albumData.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return albumData.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return trackData.get(albumData.get(i)).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    // TODO
    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        Album album = albumData.get(i);
        String albumName = album.getTitle();
        String albumArtist = album.getArtist();

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.group_view, null);
        }

        TextView album_name = (TextView) view.findViewById(R.id.album_name);
        TextView album_artist = (TextView) view.findViewById(R.id.album_artist);
        album_name.setText(albumName);
        album_artist.setText(albumArtist);
        // TODO set TypeFace here, low priority, just to make things pretty

        Button play_button = (Button) view.findViewById(R.id.play_album);
        // TODO set onClickListener

        return view;
    }

    // TODO
    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
