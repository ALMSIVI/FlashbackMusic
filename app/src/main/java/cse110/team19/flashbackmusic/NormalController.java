package cse110.team19.flashbackmusic;

import android.location.Geocoder;

import java.util.Date;
import java.util.Locale;

/**
 * Created by YueWu on 3/12/2018.
 */

public class NormalController extends MusicController {

    /**
     * Constructor.
     * @param mainActivity
     * @param adapter
     * @param player
     */
    public NormalController(MainActivity mainActivity, PlayListAdapter adapter, MusicPlayer player,
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
        changePlay();
        player.reset();
        playList.createPlayList();
        adapter.notifyDataSetChanged();
    }

    public void sortPlayList(PlayList.Sort sort) {
        playList.sort(sort);
        adapter.notifyDataSetChanged();
    }
}