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
    public ActivityTestRule<FlashbackMode> flashback = new ActivityTestRule<FlashbackMode>(FlashbackMode.class);

    int hour1 = 7;
    int hour2 = 14;
    int hour3 = 20;

    @Test
    public void testCurrentTime() {
        assertEquals("morning", flashback.getActivity().currentTime(hour1));

        assertEquals("afternoon", flashback.getActivity().currentTime(hour2));

        assertEquals("evening", flashback.getActivity().currentTime(hour3));
    }
}
