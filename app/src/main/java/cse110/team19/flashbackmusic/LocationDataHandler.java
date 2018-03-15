package cse110.team19.flashbackmusic;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * Retrieves and updates information from the location database
 * Created by Tyler on 3/14/18.
 */

public class LocationDataHandler {
    public DataSnapshot retrieve(String location) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("location");

        final DataSnapshot dataSnapshot = snapshot.child(location);


        return null;
    }

    public void update(String location, String url) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("location");
        DatabaseReference locReference = reference.child(location);
        locReference.push().setValue(url);
    }
}
