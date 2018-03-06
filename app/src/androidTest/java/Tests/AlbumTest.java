package Tests;

import org.junit.Before;
import org.junit.Test;

import cse110.team19.flashbackmusic.Track;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Meeta on 2/18/18.
 */

public class AlbumTest {
    Album album;
    Track track;

    @Before
    public void initialize() {
        album = new Album("Birthdays", "Sarah", 1);
        track = new Track("Happy Birthday", 1, "Sarah", 32);
    }

    @Test
    public void testAddTrack() {
        album.addTrack(track);
        assertEquals(track, album.getTrack("Happy Birthday"));
    }
}
