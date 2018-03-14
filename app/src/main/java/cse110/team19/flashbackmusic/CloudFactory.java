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
        // Get data from tracks reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("track");

        Query query = reference.orderByChild("trackName").equalTo(path);

        final Track track = new Track();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                    Log.w("DatabaseError", "No data found");
                } else {
                    // Get track name, time and url
                    Track tempTrack = dataSnapshot.getValue(Track.class);

                    track.setTrackName(tempTrack.getTrackName());
                    track.setTime(tempTrack.getTime());
                    track.setWebsite(tempTrack.getWebsite());

                    // Retrieve data from sharedPreferences
                    SharedPreferences sharedPreferences =
                            context.getSharedPreferences("track_info", MODE_PRIVATE);
                    int status = sharedPreferences.getInt(track.getPathName() + "Status", 0);
                    track.setStatus(status);

                    // TODO: update location and user info
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DatabaseError", databaseError.toException());
            }
        });

        return track;
    }
}
