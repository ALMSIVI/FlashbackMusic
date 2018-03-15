package cse110.team19.flashbackmusic;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Tyler on 3/14/18.
 */

public class UserDataHandler {
    public User retrieve() {
        return null;
    }

    public void update() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("users");
    }
}
