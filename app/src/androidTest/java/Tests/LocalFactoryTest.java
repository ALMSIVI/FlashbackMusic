package Tests;

import android.os.Environment;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import cse110.team19.flashbackmusic.LocalFactory;
import cse110.team19.flashbackmusic.MainActivity;
import cse110.team19.flashbackmusic.R;
import cse110.team19.flashbackmusic.Track;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static junit.framework.Assert.assertEquals;

/**
 * Created by Meeta on 2/18/18.
 */

public class LocalFactoryTest {
    Track track;
    LocalFactory localFactory;

    @Rule
    public ActivityTestRule<MainActivity> mainActivity = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Before
    public void initialize() {
        localFactory = new LocalFactory(mainActivity.getActivity());
    }

    @Test
    public void testCreateTrack() {
        String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()
                + mainActivity.getActivity().getResources().getString(R.string.download_folder)
                + "blood_on_your_bootheels.mp3";
        track = localFactory.createTrack(path);

        assertEquals("Blood On Your Bootheels", track.getTrackName());
        assertEquals("Unknown artist", track.getArtistName());
        assertEquals("I Will Not Be Afraid (A Sampler)", track.getAlbumName());
        assertEquals(1, track.getTrackNumber());
    }
}