package cse110.team19.flashbackmusic;

import java.util.Collections;
import java.util.List;

/**
 * Created by YueWu on 3/6/2018.
 */

public class PlayList {
    private List<Track> playList;

    public PlayList(List<Track> list) {
        playList = list;
    }

    public void sortRecent() {
        Collections.sort(playList, Track.recentComparator);
    }

    public void sortName() {
        Collections.sort(playList, Track.nameComparator);
    }

    public void sortAlbum() {
        Collections.sort(playList, Track.albumComparator);
    }

    public void sortArtist() {
        Collections.sort(playList, Track.artistComparator);
    }

    public void sortFavorite() {
        Collections.sort(playList, Track.favoriteComparator);
    }

    public void sortScore() {
        // TODO: Retrieve music from website, calculate scores, update playlist
        Collections.sort(playList, Track.scoreComparator);
    }
}
