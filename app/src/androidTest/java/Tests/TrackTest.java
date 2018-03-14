package Tests;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import cse110.team19.flashbackmusic.Track;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Meeta on 2/18/18.
 */

public class TrackTest {

    Track track;

    @Before
    public void initialize() {
        track = new Track("Happy Birthday", "HBD Album", "Meeta", 1, "path");
    }

    @Test
    public void testUpdateStatus() {
        track.updateStatus();
        assertEquals(1, track.getStatus());

        track.updateStatus();
        assertEquals(-1, track.getStatus());

        track.updateStatus();
        assertEquals(0, track.getStatus());
    }

    @Test
    public void testIncrementScore() {
        track.incrementScore(5);
        assertEquals(5, track.getScore());
    }
}
