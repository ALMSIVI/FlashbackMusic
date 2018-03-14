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
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Get data from tracks reference
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
                    int year = 0;
                    int month = 0;
                    int day = 0;
                    int hour = 0;
                    int minute = 0;
                    double latitude = 0;
                    double longitude = 0;

                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        switch (child.getKey()) {
                            case "trackName":
                                track.setTrackName((String)child.getValue());
                                break;
                            case "website":
                                track.setWebsite((String)child.getValue());
                                break;
                            case "year":
                                year = (Integer)child.getValue();
                                break;
                            case "month":
                                month = (Integer)child.getValue();
                                break;
                            case "day":
                                day = (Integer)child.getValue();
                                break;
                            case "hour":
                                hour = (Integer)child.getValue();
                                break;
                            case "minute":
                                minute = (Integer)child.getValue();
                                break;
                            case "latitude":
                                latitude = (Double)child.getValue();
                                break;
                            case "longitude":
                                longitude = (Double)child.getValue();
                        }
                    }

                    track.setDate(year, month, day, hour, minute);
                    track.setLocation(latitude, longitude);

                    // Retrieve data from sharedPreferences
                    SharedPreferences sharedPreferences =
                            context.getSharedPreferences("track_info", MODE_PRIVATE);
                    int status = sharedPreferences.getInt(track.getPathName() + "Status", 0);
                    track.setStatus(status);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DatabaseError", databaseError.toException());
            }
        });

        // TODO: retrieve user and location info

        return track;
    }
}
