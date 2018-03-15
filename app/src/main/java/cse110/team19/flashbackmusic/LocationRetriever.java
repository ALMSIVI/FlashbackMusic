package cse110.team19.flashbackmusic;

import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Tyler on 3/13/18.
 */

public class LocationRetriever implements Retriever {
    @Override
    public Object retrieve() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Get data from tracks reference
        DatabaseReference reference = database.getReference("locationwrapper");

        //Query query = reference.orderByChild("location").equalTo(path);

        final Location location = new Location("");


        return null;
    }

    @Override
    public Object retrieve(Object input) {
        return null;
    }
}
