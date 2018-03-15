package cse110.team19.flashbackmusic;

import android.location.Location;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;


/**
 * Created by Meeta on 3/6/18.
 */

public class MusicPlayer {
    //region Variables
    private MediaPlayer mediaPlayer;

    // MVC
    private MusicController controller;

    //endregion

    /**
     * Constructor
     * @param mp MediaPlayer
     */
    public MusicPlayer(MediaPlayer mp) {
        mediaPlayer = mp;

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {


                controller.updateTrackInfo();
                controller.saveTrackInfo(true, controller.getIsPlaying());

                // TODO: THIS STUFF <-- Put in updateTrackInfo
                // once a song is completed, add that song to the location it was played at
                // Location location = gpsTracker.getLocation();
                // reverse geocode here
                // locationDataHandler.update(location, track name);

                if (!controller.isNormalMode()) {
                    playNext();
                }
            }
        });
    }

    public void setController(MusicController controller) {
        this.controller = controller;
    }

    /**
     * Plays current song.
     */
    public void play() {
        mediaPlayer.start();
    }

    /**
     * Pauses playing song.
     */
    public void pause() {
        mediaPlayer.pause();
    }

    /**
     * Stops the song.
     */
    public void stop() {
        mediaPlayer.stop();
    }

    /**
     * Request next song from controller and play that.
     */
    public void playNext() {
        Track next = controller.getNext();
        stop();
        if (next != null) {
            if (next.getStatus() != -1) {
                setDataSource(next);
                prepareAsync();
            } else {
                playNext();
            }
        } else {
            controller.changePlay();
        }
    }

    /**
     * Checks if player is playing.
     * @return true - playing
     *         false - not playing
     */
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    /**
     * Reset music.
     */
    public void resetMusic() {
        mediaPlayer.seekTo(0);
    }

    public void reset() {
        mediaPlayer.reset();
    }

    public void setDataSource(Track track) {
        try {
            if (track != null) {
                mediaPlayer.setDataSource(track.getPathName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void prepareAsync() {
        mediaPlayer.prepareAsync();
    }
}
