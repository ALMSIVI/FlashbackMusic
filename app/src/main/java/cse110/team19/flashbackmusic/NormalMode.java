package cse110.team19.flashbackmusic;

import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * This is the normal mode window.
 */
public class NormalMode extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    ArrayList<Integer> audioResourceId = new ArrayList();
    int audioIndex = 0;
    boolean songHasLoaded = false;

    public void loadSongs() {
        final Field[] fields = R.raw.class.getFields();

        for (int count = 0; count < fields.length; count++) {
            String name = fields[count].getName();
            int resourceID = getResources().getIdentifier(name, "raw", getPackageName());
            audioResourceId.add(resourceID);
        }
    }

    public void loadMedia(int resourceId) {
        mediaPlayer.reset();
        AssetFileDescriptor assetFileDescriptor = this.getResources().openRawResourceFd(resourceId);
        try {
            mediaPlayer.setDataSource(assetFileDescriptor);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        audioIndex++;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_mode);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        loadSongs();

        mediaPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if (audioResourceId.size() > audioIndex) {
                            loadMedia(audioResourceId.get(audioIndex));
                            //mediaPlayer.start();
                            //mediaPlayer.
                        }
                    }
                }
        );

        mediaPlayer.setOnPreparedListener(
                new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                }
        );

        //loadMedia(MEDIA_RES_ID);
        final Button playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Check if something is already playing
                        if(mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            playButton.setBackgroundResource(android.R.drawable.ic_media_play);
                        } else {

                            if (songHasLoaded == false) {
                                loadMedia(audioResourceId.get(audioIndex));
                                songHasLoaded = true;
                            }

                            //Since there is already a song loaded, just resume the song
                            mediaPlayer.start();
                            playButton.setBackgroundResource(android.R.drawable.ic_media_pause);
                        }
                    }
                }
        );
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isChangingConfigurations() && mediaPlayer.isPlaying()) {
            ; //"do nothing"
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_normal_mode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
