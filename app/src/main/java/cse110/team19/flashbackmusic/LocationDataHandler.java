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
        DatabaseReference locReference = reference.child(location);

        /*FirebaseDatabase.getInstance().getReference().child("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            System.out.println(user.email);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });*/

        /*var ref = firebase.database().ref("users/ada");
        ref.once("value")
                .then(function(snapshot) {
            var key = snapshot.key; // "ada"
            var childKey = snapshot.child("name/last").key; // "last"
        });*/

        return null;
    }

    public void update(String location, String url) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("location");
        DatabaseReference locReference = reference.child(location);
        locReference.push().setValue(url);
    }
}
