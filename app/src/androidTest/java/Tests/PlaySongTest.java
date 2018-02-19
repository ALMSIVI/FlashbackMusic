package Tests;

import android.support.test.rule.ActivityTestRule;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import cse110.team19.flashbackmusic.NormalMode;
import cse110.team19.flashbackmusic.R;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Meeta on 2/18/18.
 */

public class PlaySongTest {
    @Rule
    public ActivityTestRule<NormalMode> normal = new ActivityTestRule<NormalMode>(NormalMode.class);

    private ExpandableListView elv;
    private TextView tv;

    @Before
    public void openListView() {
        elv = (ExpandableListView) normal.getActivity().findViewById(R.id.expandableListView);
        elv.expandGroup(1);
    }

    @Test
    public void test1() {
        tv = normal.getActivity().findViewById(R.id.album_name);
        //tv = (TextView) normal.getActivity().findViewById(R.id.track_name);
        String blood = tv.getText().toString();

        assertEquals("I Will Not Be Afraid (A Sampler)", blood);
    }
}
