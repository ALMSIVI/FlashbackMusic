package cse110.team19.flashbackmusic;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by YueWu on 3/14/2018.
 */

public class TrackDataHandler {
    private Context context;
    public TrackDataHandler(Context context) {
        this.context = context;
    }

    /**
     * @require track.getTrackName() != null
     * @param track
     */
    public void retrieveTrack(final Track track) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Get data from tracks reference
        DatabaseReference reference = database.getReference("track");

        Query query = reference.orderByChild("trackName").equalTo(track.getTrackName());

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

                    dataSnapshot = dataSnapshot.child(track.getTrackName());
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        switch (child.getKey()) {
                            case "trackName":
                                track.setTrackName((String)child.getValue());
                                break;
                            case "website":
                                track.setWebsite((String)child.getValue());
                                break;
                            case "year":
                                year = ((Long)child.getValue()).intValue();
                                break;
                            case "month":
                                month = ((Long)child.getValue()).intValue();
                                break;
                            case "day":
                                day = ((Long)child.getValue()).intValue();
                                break;
                            case "hour":
                                hour = ((Long)child.getValue()).intValue();
                                break;
                            case "minute":
                                minute = ((Long)child.getValue()).intValue();
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
                            context.getSharedPreferences("tracks", MODE_PRIVATE);
                    int status = sharedPreferences.getInt(track.getPathName() + "Status", 0);
                    track.setStatus(status);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DatabaseError", databaseError.toException());
            }
        });
    }

    public void writeTrack(Track track) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("track");

        Log.d("sharedpreference now website", track.getWebsite());

        reference = reference.child(track.getTrackName());

        Map<String, Object> trackInfo = new HashMap<String, Object>();
        trackInfo.put("trackName", track.getTrackName());
        trackInfo.put("website", track.getWebsite());

        LocalDateTime dateTime = track.getDate();
        trackInfo.put("year", dateTime.getYear());
        trackInfo.put("month", dateTime.getMonthValue());
        trackInfo.put("day", dateTime.getDayOfMonth());
        trackInfo.put("hour", dateTime.getHour());
        trackInfo.put("minute", dateTime.getMinute());

        Location location = track.getLocation();
        trackInfo.put("latitude", location.getLatitude());
        trackInfo.put("longitude", location.getLongitude());

        // TODO: put person
        reference.updateChildren(trackInfo);
    }

    /**
     * Gets only the time, location and user data.
     * @require track.getTrackName() != null
     * @param track
     */
    public void retrieveTrackNecessary(final Track track) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Get data from tracks reference
        DatabaseReference reference = database.getReference("track");

        Query query = reference.orderByChild("trackName").equalTo(track.getTrackName());

        Log.d("track name", track.getTrackName());
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

                    dataSnapshot = dataSnapshot.child(track.getTrackName());
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Log.d("key", child.getKey());
                        switch (child.getKey()) {
                            case "year":
                                year = ((Long)child.getValue()).intValue();
                                break;
                            case "month":
                                month = ((Long)child.getValue()).intValue();
                                break;
                            case "day":
                                day = ((Long)child.getValue()).intValue();
                                break;
                            case "hour":
                                hour = ((Long)child.getValue()).intValue();
                                break;
                            case "minute":
                                minute = ((Long)child.getValue()).intValue();
                                break;
                            case "latitude":
                                latitude = (Double) child.getValue();
                                break;
                            case "longitude":
                                longitude = (Double) child.getValue();
                        }
                    }

                    if (month != 0) {
                        track.setDate(year, month, day, hour, minute);
                    }

                    if (latitude != 0 || longitude != 0) {
                        track.setLocation(latitude, longitude);
                    }
                    // Retrieve data from sharedPreferences
                    SharedPreferences sharedPreferences =
                            context.getSharedPreferences("tracks", MODE_PRIVATE);
                    int status = sharedPreferences.getInt(track.getPathName() + "Status", 0);
                    track.setStatus(status);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DatabaseError", databaseError.toException());
            }
        });
    }
}
