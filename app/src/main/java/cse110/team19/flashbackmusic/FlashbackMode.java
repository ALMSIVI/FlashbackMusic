package cse110.team19.flashbackmusic;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FlashbackMode extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private TreeMap<Integer, Track> playList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //This needs to go before the button
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashback_mode);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void switchNormal(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_name", MODE_PRIVATE);
        String mode = sharedPreferences.getString("mode", "");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("mode", "Normal");
        editor.apply();
        finish();
    }

    public void createFlashback(Map<Album, List<Track>> input) {
        playList = new TreeMap<Integer, Track>(
                new Comparator<Integer>() {
                    @Override
                    public int compare(Integer integer, Integer t1) {
                        return t1.compareTo(integer);
                    }
                }
        );

        Calendar calender;
        calender = Calendar.getInstance();
        int currentHour = calender.get(Calendar.HOUR_OF_DAY);
        int currentDay = calender.get(Calendar.DAY_OF_WEEK);
        String timeOfDay = currentTime(currentHour);


        //For each album
        for(Map.Entry<Album, List<Track>> entry : input.entrySet()) {
            //This is the list of tracks
            List<Track> currentList = entry.getValue();

            //For each track
            for (Track track : currentList) {

                //Check time of day
                if(track.getTimePlayed().equals(timeOfDay)) {
                    track.incrementScore(5);
                }

                //Check day of week
                if(track.getDayPlayed() == (currentDay)) {
                    track.incrementScore(5);
                }

                //Get status
                int status = track.getStatus();

                if(status == 1) {
                    track.incrementScore(1);
                } else if(status == -1) {
                    track.makeScoreZero();
                }

                if((track.getScore() != 0)) {
                    playList.put(track.getScore(), track);
                }

            }
        }
    }

    public String currentTime(int hour) {
        if( 5 <= hour && hour < 11) {
            return "morning";
        }

        if( 11 <= hour && hour < 17 ) {
            return "afternoon";
        } else {
            return "evening";
        }
    }

    public void resetMusic(View view) {
        mediaPlayer.seekTo(0);
    }

    public void playMusic(View view) {

    }
}
