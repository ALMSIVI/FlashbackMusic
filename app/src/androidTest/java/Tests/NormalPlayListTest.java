package Tests;

import android.os.Environment;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import cse110.team19.flashbackmusic.MainActivity;
import cse110.team19.flashbackmusic.NormalPlayList;
import cse110.team19.flashbackmusic.R;
import cse110.team19.flashbackmusic.Track;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static junit.framework.Assert.assertEquals;

/**
 * Created by Meeta on 3/14/18.
 */


public class NormalPlayListTest {
    NormalPlayList normalPlayList;
    Track track;

    @Rule
    public ActivityTestRule<MainActivity> mainActivity = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Before
    public void initialize() {
        String folder = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()
                + mainActivity.getActivity().getResources().getString(R.string.download_folder);
        normalPlayList = new NormalPlayList(mainActivity.getActivity(), folder);
    }

    @Test
    public void testCreatePlayList() {
        normalPlayList.createPlayList();

        track = normalPlayList.get(0);
        assertEquals("Windows Are The Eyes To The House", track.getTrackName());
    }
}
