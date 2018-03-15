package Tests;

import android.media.MediaPlayer;
import android.os.Environment;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import cse110.team19.flashbackmusic.LocalFactory;
import cse110.team19.flashbackmusic.MainActivity;
import cse110.team19.flashbackmusic.MusicPlayer;
import cse110.team19.flashbackmusic.NormalController;
import cse110.team19.flashbackmusic.NormalPlayList;
import cse110.team19.flashbackmusic.PlayListAdapter;
import cse110.team19.flashbackmusic.R;
import cse110.team19.flashbackmusic.Track;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static junit.framework.Assert.assertEquals;

/**
 * Created by Meeta on 2/18/18.
 */

public class MusicPlayerTest {

    MusicPlayer mp;
    Track track;
    LocalFactory localFactory;

    @Rule
    public ActivityTestRule<MainActivity> mainActivity = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);


    @Before
    public void initialize() {
        mp = new MusicPlayer(new MediaPlayer());

        localFactory = new LocalFactory(mainActivity.getActivity());
        String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()
                + mainActivity.getActivity().getResources().getString(R.string.download_folder) +
                "blood_on_your_bootheels.mp3";
        track = localFactory.createTrack(path);

        PlayListAdapter playListAdapter = new PlayListAdapter(mainActivity.getActivity());
        NormalPlayList normalPlayList = new NormalPlayList(mainActivity.getActivity(), Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()
                + mainActivity.getActivity().getResources().getString(R.string.download_folder));

        mp.setController(new NormalController(mainActivity.getActivity(), playListAdapter, mp, normalPlayList));
    }

    @Test
    public void testPlayPause() {
        mp.setDataSource(track);

        mp.play();
        assertEquals(false, mp.isPlaying());

        mp.pause();
        assertEquals(false, mp.isPlaying());

        mp.play();
        mp.stop();
        assertEquals(false, mp.isPlaying());
    }
}