package Tests;

import android.app.DownloadManager;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import cse110.team19.flashbackmusic.Download;
import cse110.team19.flashbackmusic.MainActivity;
import cse110.team19.flashbackmusic.R;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * Created by Meeta on 3/14/18.
 */

public class DownloadTest {
    Download download;
    DownloadManager dm;
    String url = "";

    @Rule
    public ActivityTestRule<MainActivity> mainActivity = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Before
    public void initialize() {
        DownloadManager dm = (DownloadManager)mainActivity.getActivity().getSystemService(DOWNLOAD_SERVICE);
        download = new Download(dm, mainActivity.getActivity().getResources().getString(R.string.download_folder));
    }

    @Test
    public void testDownload() {

    }
}
