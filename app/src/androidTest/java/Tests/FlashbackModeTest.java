package Tests;

import android.support.test.rule.ActivityTestRule;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import cse110.team19.flashbackmusic.FlashbackMode;
import cse110.team19.flashbackmusic.NormalMode;
import cse110.team19.flashbackmusic.R;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Meeta on 2/18/18.
 */

public class FlashbackModeTest {
    @Rule
    public ActivityTestRule<FlashbackMode> normal = new ActivityTestRule<FlashbackMode>(FlashbackMode.class);

    @Before
    public void openListView() {

    }

    @Test
    public void test1() {

    }
}
