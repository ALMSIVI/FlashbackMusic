package cse110.team19.flashbackmusic;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by YueWu on 3/6/2018.
 */

public class PlayList {
    private TrackFactory factory;
    private List<Track> playList;
    private String downloadFolder;
    private Context context;

    public enum Sort {
        Recent, Name, Album, Artist, Favorite, Score;
    }

    Sort mode = Sort.Recent;

    public PlayList(Context cont, String folder) {
        context = cont;
        downloadFolder = folder;
        playList = new ArrayList<Track>();
    }

    public void createNormalPlayList() {
        playList.clear();
        factory = new LocalFactory(context);
        // getting songs out of Downloads folder
        Log.d("Files", "Path: " + downloadFolder);
        File directory = new File(downloadFolder);
        File[] fields = directory.listFiles();
        Log.d("Files", "Size: " + fields.length);
        for (int i = 0; i < fields.length; i++) {
            Log.d("Files", "FileName:" + fields[i].getName());
        }

        for (int count = 0; count < fields.length; count++) { //Goes through each track
            addTrack(fields[count].getName());
        }

        sortRecent();
    }

    public void createVibePlayList() {
        playList.clear();
        // TODO: retrieve every song info from Firebase
        factory = new CloudFactory(context);

        Calendar calender;
        calender = Calendar.getInstance();

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

        sortScore();
    }

    public void addTrack(String filename) {
        Track t = factory.createTrack(downloadFolder + filename);
        playList.add(t);
        // TODO: see if cloud generated songs already exist in local
    }


    public int size() {
        return playList.size();
    }

    public Track get(int i) {
        return playList.get(i);
    }

    public boolean isNormalMode() {
        return mode != Sort.Score;
    }

    //region Sorters
    public void sort() {
        switch (mode) {
            case Name:
                sortName();
                break;
            case Album:
                sortAlbum();
                break;
            case Artist:
                sortArtist();
                break;
            case Recent:
                sortRecent();
                break;
            case Favorite:
                sortFavorite();
                break;
            case Score:
            default:
                break;
        }
    }

    public void sortRecent() {
        mode = Sort.Recent;
        Collections.sort(playList, Track.recentComparator);
    }

    public void sortName() {
        mode = Sort.Name;
        Collections.sort(playList, Track.nameComparator);
    }

    public void sortAlbum() {
        mode = Sort.Album;
        Collections.sort(playList, Track.albumComparator);
    }

    public void sortArtist() {
        mode = Sort.Artist;
        Collections.sort(playList, Track.artistComparator);
    }

    public void sortFavorite() {
        mode = Sort.Favorite;
        Collections.sort(playList, Track.favoriteComparator);
    }

    public void sortScore() {
        // TODO: Retrieve music from website, calculate scores, update playlist
        mode = Sort.Score;
        Collections.sort(playList, Track.scoreComparator);
    }
}
