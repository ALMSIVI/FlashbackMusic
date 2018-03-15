package cse110.team19.flashbackmusic;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by YueWu on 3/12/2018.
 */

public class VibePlayList extends PlayList {
    private TrackFactory factory;
    private String downloadFolder;
    private Context context;


    public VibePlayList(Context cont, String folder) {
        context = cont;
        downloadFolder = folder;
        playList = new ArrayList<Track>();
        factory = new CloudFactory(context);
    }

    @Override
    public void createPlayList() {
        playList.clear();
        // TODO: retrieve every song info from Firebase

        //For each track
        for (Track track : playList) {
            // TODO: Criterion check

            //Get status
            int status = track.getStatus();

            if (status == 1) {
                track.incrementScore(100);
            } else if (status == -1) {
                track.makeScoreNegative();
            }

            if(track.getScore() > -1) {
                playList.add(track);
            }
        }

        sort();
    }

    @Override
    public void addTrack(String filename) {
        // TODO: see if cloud generated songs already exist in local
    }

    @Override
    public boolean isNormalMode() {
        return false;
    }

    @Override
    public void sort() {
        // TODO: Retrieve music from website, calculate scores, update playlist
        Collections.sort(playList, Track.scoreComparator);
    }

    /**
     * Reject any sort requests and sort based on score.
     * @param sort
     */
    @Override
    public void sort(PlayList.Sort sort) {
        sort();
    }
}
