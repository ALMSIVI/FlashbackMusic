package cse110.team19.flashbackmusic;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by YueWu on 3/12/2018.
 */

public class NormalPlayList extends PlayList {
    private TrackFactory factory;
    private String downloadFolder;
    private Context context;


    Sort mode = Sort.Recent;

    public NormalPlayList(Context cont, String folder) {
        context = cont;
        downloadFolder = folder;
        playList = new ArrayList<Track>();
        factory = new LocalFactory(context);
    }

    @Override
    public void createPlayList() {
        playList.clear();
        // getting songs out of Downloads folder
        Log.d("Files", "Path: " + downloadFolder);
        File directory = new File(downloadFolder);
        File[] fields = directory.listFiles();
        if (fields != null) {
            Log.d("Files", "Size: " + fields.length);
            for (int i = 0; i < fields.length; i++) {
                Log.d("Files", "FileName:" + fields[i].getName());
            }

            for (int count = 0; count < fields.length; count++) { //Goes through each track
                addTrack(fields[count].getName());
            }

            sortRecent();
        }
    }

    @Override
    public boolean isNormalMode() {
        return true;
    }

    @Override
    public void addTrack(String filename) {
        if (factory instanceof LocalFactory) {
            Track t = factory.createTrack(downloadFolder + filename);
            playList.add(t);
        }
        // TODO: see if cloud generated songs already exist in local
    }

    @Override
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


    public void sort(Sort sort) {
        switch (sort) {
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

    private void sortRecent() {
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

}
