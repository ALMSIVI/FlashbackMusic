package cse110.team19.flashbackmusic;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by YueWu on 3/6/2018.
 */

public abstract class PlayList {
    public enum Sort {
        Recent, Name, Album, Artist, Favorite, Score
    }

    protected List<Track> playList;

    public int size() {
        return playList.size();
    }

    public Track get(int i) {
        return playList.get(i);
    }

    //region Abstract Methods
    public abstract void createPlayList();

    public abstract void addTrack(String filename);

    public abstract boolean isNormalMode();

    public abstract void sort();

    public abstract void sort(Sort sort);
    //endregion
}
