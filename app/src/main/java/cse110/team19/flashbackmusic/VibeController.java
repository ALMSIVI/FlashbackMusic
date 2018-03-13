package cse110.team19.flashbackmusic;

import android.location.Geocoder;

import java.util.Date;
import java.util.Locale;

/**
 * Created by YueWu on 3/12/2018.
 */

public class VibeController extends MusicController {
    /**
     * Constructor.
     * @param mainActivity
     * @param adapter
     * @param player
     */
    public VibeController(MainActivity mainActivity, PlayListAdapter adapter, MusicPlayer player,
                           PlayList playList) {
        this.mainActivity = mainActivity;
        this.adapter = adapter;
        adapter.setController(this);
        this.player = player;
        player.setController(this);
        this.playList = playList;
        // Initialize the locations
        geocoder = new Geocoder(mainActivity, Locale.getDefault());
        gpstracker = new GPSTracker(mainActivity);
        //location = gpstracker.getLocation();
        time = new Date();
    }

    @Override
    public boolean isNormalMode() {
        return true;
    }

    @Override
    public void setUp() {
        playList.createPlayList();
        adapter.notifyDataSetChanged();
        isPlaying = -1;
        player.stop();
    }
}
