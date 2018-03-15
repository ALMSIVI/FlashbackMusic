package cse110.team19.flashbackmusic;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by YueWu on 3/12/2018.
 */

public class CloudFactory implements TrackFactory {
    private Context context;

    public CloudFactory(Context context) {
        this.context = context;
    }

    /**
     * Creates a track from Firebase.
     * @param path the URL of the song
     * @return a cloud song
     */
    public Track createTrack(String path) {
        Track track = new Track();
        track.setTrackName(path);

        TrackDataHandler handler = new TrackDataHandler(context);
        handler.retrieveTrack(track);
        return track;
    }
}
