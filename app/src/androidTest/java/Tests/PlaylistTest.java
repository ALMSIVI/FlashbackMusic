package Tests;

import org.junit.Before;
import org.junit.Test;

import cse110.team19.flashbackmusic.Track;
import cse110.team19.flashbackmusic.PlayList;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Meeta on 3/12/18.
 */

public class PlaylistTest {

    Track track1, track2, track3;
    PlayList playList;

    @Before
    public void initialize() {
        track1 = new Track("Happy Birthday", "HBD Album", "Meeta", 1, "path");

        //playList.addTrack(track1);
    }

    @Test
    public void testUpdateStatus() {

    }

    @Test
    public void testIncrementScore() {

    }
}