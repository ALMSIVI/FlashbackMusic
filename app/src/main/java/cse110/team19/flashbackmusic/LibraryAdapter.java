package cse110.team19.flashbackmusic;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private MediaPlayer mediaPlayer;
    private ArrayList<Integer> audioResourceId;
    private int audioIndex = 0;
    private Track isPlaying;

    /**
     * Constructor.
     * @param c
     * @param l
     * @param h
     * @param m
     */
    public LibraryAdapter(Context c, List<Album> l, Map<Album, List<Track>> h, MediaPlayer m) {
        context = c;
        albumData = l;
        trackData = h;
        mediaPlayer = m;

        mediaPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if (audioResourceId.size() > audioIndex) {
                            Log.d("hi", "woah");
                            loadMedia(audioResourceId.get(audioIndex));
                            //isPlaying = listOfTracks.get(audioIndex);
                            //mediaPlayer.start();
                            //mediaPlayer.
                        }
                    }
                }
        );

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                TextView infoView = ((Activity) context).findViewById(R.id.info);
                infoView.setText(isPlaying.getTrackName());
                TextView lastPlayedView = ((Activity) context).findViewById(R.id.lastPlayed);
                if (isPlaying.getCalendar() == null) {
                    //lastPlayedView.setText(context.getString(R.id.never_played_info));
                } else {
                    String lastPlayedInfo = String.format(
                            context.getString(R.string.last_played_info),
                            isPlaying.getCalendar().getTime().toString(), "Dummy", "Dummy");
                    lastPlayedView.setText(lastPlayedInfo);
                }
            }
        });
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


    public void changePlayPause(View view)
    {

        Button mainPlayButton = (Button) ((Activity)context).findViewById(R.id.playButton);
        Drawable pause = context.getResources().getDrawable(R.drawable.ic_pause_actuallyblack_24dp);
        mainPlayButton.setCompoundDrawablesWithIntrinsicBounds(null, pause, null, null);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        Album album = (Album) getGroup(i);
        String albumName = album.getTitle();
        String albumArtist = album.getArtist();
        audioResourceId = new ArrayList<Integer>();
        final List<Track> listOfTracks = trackData.get(album);

        // Sort the tracks based on track number
        Collections.sort(listOfTracks, new Comparator<Track>() {
            @Override
            public int compare (Track t1, Track t2) {
                return t1.getTrackNumber() - t2.getTrackNumber();
            }
        });
        //final Track[] trackArray = n

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
        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioIndex = 0;
                changePlayPause(view);
                //ArrayList<Integer> audioResourceId = new ArrayList<Integer>();

                for (Track t : listOfTracks) {
                    if (t.getStatus() > -1) {
                        int id = t.getResourceId();
                        audioResourceId.add(id);
                        Log.d("trackname", t.getTrackName());
                        Log.d("track number", t.getTrackNumber() + "");
                        mediaPlayer.reset();
                        AssetFileDescriptor assetFileDescriptor = context.getResources().openRawResourceFd(id);
                        try {
                            mediaPlayer.setDataSource(assetFileDescriptor);
                            mediaPlayer.prepareAsync();
                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                    }
                }
                loadMedia(audioResourceId.get(audioIndex));
                isPlaying = listOfTracks.get(audioIndex - 1);
                //}
            }
        });
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        final Track track = (Track) getChild(i, i1);

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.child_view, null);
        }

        TextView track_name = (TextView) view.findViewById(R.id.track_name);
        track_name.setText(track.getTrackName());
        track_name.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                int id = track.getResourceId();
                loadMedia(id);
                isPlaying = track;
            }
        });
        // TODO set TypeFace here, low priority, just to make things pretty

        final Button status_button = (Button) view.findViewById(R.id.set_status);
        status_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                track.updateStatus();
                int stat = track.getStatus();

                if( stat == 0 )
                {
                    Drawable neutral = context.getResources().getDrawable(R.drawable.neutral);
                    status_button.setCompoundDrawablesWithIntrinsicBounds(null, neutral, null, null);
                }
                else if( stat == 1 )
                {
                    Drawable liked = context.getResources().getDrawable(R.drawable.favorite);
                    status_button.setCompoundDrawablesWithIntrinsicBounds(null, liked, null, null);
                }
                else if( stat == -1 )
                {
                    Drawable disliked = context.getResources().getDrawable(R.drawable.dislike);
                    status_button.setCompoundDrawablesWithIntrinsicBounds(null, disliked, null, null);
                }
                //for (Track t : trackArray)
            }
        });
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    /**
     * Load one media file into the player.
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
}