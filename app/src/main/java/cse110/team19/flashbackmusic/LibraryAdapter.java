package cse110.team19.flashbackmusic;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;

import static android.content.Context.MODE_PRIVATE;

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
    private ArrayList<Pair<Integer, Track>> audioResourceId;
    private int audioIndex = 0;
    private Track isPlaying;
    private Date time;

    // for recording location of song
    Geocoder geocoder;
    List<Address> addresses;
    GPSTracker gpstracker;
    Location location;

    /**
     * Constructor.
     *
     * @param c NormalMode Activity
     * @param l list of albums
     * @param h albums and their tracks
     * @param m media player
     */
    public LibraryAdapter(Context c, List<Album> l, Map<Album, List<Track>> h, MediaPlayer m) {
        context = c;
        albumData = l;
        trackData = h;
        mediaPlayer = m;
        // Initialize the locations
        geocoder = new Geocoder(context, Locale.getDefault());
        gpstracker = new GPSTracker(context);
        location = gpstracker.getLocation();
        time = new Date();

        mediaPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        location = gpstracker.getLocation();

                        isPlaying.updateInfo(location, time.getTime());

                        saveTrackInfo(true, isPlaying);

                        if (audioResourceId.size() > audioIndex) {
                            loadMedia(audioResourceId.get(audioIndex).first, audioResourceId.get(audioIndex).second);
                        } else {
                            updateText();
                            changePausePlay();
                        }
                    }
                }
        );

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
    }

    public Track getIsPlaying() {
        return isPlaying;
    }

    private void changePlayPause() {
        Button mainPlayButton = (Button) ((Activity) context).findViewById(R.id.playButton);
        Drawable pause = context.getResources().getDrawable(R.drawable.ic_pause_actuallyblack_24dp);
        mainPlayButton.setCompoundDrawablesWithIntrinsicBounds(null, pause, null, null);
    }

    private void changePausePlay() {
        Button mainPauseButton = (Button) ((Activity) context).findViewById(R.id.playButton);
        Drawable play = context.getResources().getDrawable(R.drawable.ic_play_arrow_actuallyblack_24dp);
        mainPauseButton.setCompoundDrawablesWithIntrinsicBounds(null, play, null, null);
    }


    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        Album album = (Album) getGroup(i);
        String albumName = album.getTitle();
        String albumArtist = album.getArtist();

        final List<Track> listOfTracks = trackData.get(album);

        // Sort the tracks based on track number
        Collections.sort(listOfTracks, new Comparator<Track>() {
            @Override
            public int compare(Track t1, Track t2) {
                return t1.getTrackNumber() - t2.getTrackNumber();
            }
        });

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
                audioResourceId = new ArrayList<Pair<Integer, Track>>();
                audioIndex = 0;
                changePlayPause();
                //ArrayList<Integer> audioResourceId = new ArrayList<Integer>();

                for (Track t : listOfTracks) {
                    Log.d("audioIndex", audioIndex + "");

                    if (t.getStatus() > -1) {
                        int id = t.getResourceId();
                        audioResourceId.add(new Pair<Integer, Track>(id, t));
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

                loadMedia(audioResourceId.get(audioIndex).first, audioResourceId.get(audioIndex).second);
            }
        });
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        final Track track = (Track) getChild(i, i1);

        updateTrackInfo(track);

        // inflate the view
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.child_view, null);
        }

        // When we click on the textfield, play the song
        TextView track_name = (TextView) view.findViewById(R.id.track_name);
        track_name.setText(track.getTrackName());
        track_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (track.getStatus() > -1) {
                    changePlayPause();
                    int id = track.getResourceId();
                    audioResourceId = new <Pair<Integer, Track>>ArrayList();
                    audioResourceId.add(new Pair<Integer, Track>(id, track));
                    loadMedia(id, track);
                }
            }
        });

        // TODO set TypeFace here, low priority, just to make things pretty

        // When we click on the button, set the status
        final Button status_button = (Button) view.findViewById(R.id.set_status);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        changeButton(track, status_button);

        status_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean all = false;
                track.updateStatus();
                saveTrackInfo(all, track);
                changeButton(track, status_button);

                if (track.getStatus() == -1 && mediaPlayer.isPlaying() && isPlaying == track) {
                    mediaPlayer.stop();
                    if (audioResourceId.size() > audioIndex) {
                        loadMedia(audioResourceId.get(audioIndex).first, audioResourceId.get(audioIndex).second);
                    }
                }
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
    public void loadMedia(int resourceId, Track t) {
        isPlaying = t;
        changePlayPause();
        mediaPlayer.reset();
        AssetFileDescriptor assetFileDescriptor = context.getResources().openRawResourceFd(resourceId);
        try {
            mediaPlayer.setDataSource(assetFileDescriptor);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        updateText();

        audioIndex++;
    }

    private void updateTrackInfo(Track t) {
        // Retrieve data from sharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        int status = sharedPreferences.getInt(t.getTrackName() + "Status", 0);
        t.setStatus(status);

        // calendar
        String cal = sharedPreferences.getString(t.getTrackName() + "Time", null);
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        if (cal != null) {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(format.parse(cal));
                t.setCalendar(calendar);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // location
        String loc = sharedPreferences.getString(t.getTrackName() + "Location", "Unknown Location");
        if (!loc.equals("Unknown Location")) {
            String[] locationValue = loc.split("");
            double latitude = Double.parseDouble(locationValue[0]);
            double longitude = Double.parseDouble(locationValue[1]);
            Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            t.setLocation(location);
        }
    }

    private void updateText() {
        // Update the "Now playing" text
        TextView infoView = ((Activity) context).findViewById(R.id.info);
        infoView.setText(isPlaying.getTrackName());
        // Update the "Last played" text
        TextView lastPlayedView = ((Activity) context).findViewById(R.id.lastPlayed);
        if (isPlaying.getTime() == null) {
            lastPlayedView.setText(context.getString(R.string.never_played_info));
        } else {
            String lastLocation = "Unknown location";

            if (location != null) {
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                lastLocation = addresses.get(0).getFeatureName();
            }

            String lastPlayedInfo = String.format(
                    context.getString(R.string.last_played_info),
                    isPlaying.getTime(), lastLocation);
            lastPlayedView.setText(lastPlayedInfo);
        }
    }

    /**
     * Store the current song's info into sharedPreferences. This method does NOT update song's info.
     */
    private void saveTrackInfo(boolean all, Track track) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(track.getTrackName() + "Status", track.getStatus());

        if(all) {
            editor.putString(track.getTrackName() + "Time", track.getTime());
            editor.putString(track.getTrackName() + "Location", track.getLocation());
        }
        editor.apply();
    }

    /* Overridden methods */
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

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}