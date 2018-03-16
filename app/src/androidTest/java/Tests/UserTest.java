package Tests;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.LinkedList;

import cse110.team19.flashbackmusic.Track;
import cse110.team19.flashbackmusic.User;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Meeta on 3/14/18.
 */

public class UserTest {
    User user;
    Track track1, track2;
    LinkedList<Track> list;

    @Before
    public void initialize() {
        user = new User("Sarah", "sarah@gmail.com");
        track1 = new Track();
        track2 = new Track();

        list = new LinkedList<Track>();
        list.addLast(track1);
        list.addLast(track2);

        user.tracks = list;
    }

    @Test
    public void testGetLatestTrack() {
        Track test = user.getLatestTrack();
        assertEquals(track2, test);
    }
}
