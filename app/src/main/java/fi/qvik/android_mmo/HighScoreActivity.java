package fi.qvik.android_mmo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tommy on 06/03/2018.
 */

public class HighScoreActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private final String FIREBASE_PLAYERS = "https://android-mmo.firebaseio.com/players";
    private FirebaseDatabase database;
    private Query playersRef;

    private RecyclerView recyclerView;
    private HighScoreAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.high_score_activity);
        database = FirebaseDatabase.getInstance();
        // TODO: proper ordering using firebase would require player scores had structure and named score field
        playersRef = database.getReferenceFromUrl(FIREBASE_PLAYERS);
        playersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Object storedValue = dataSnapshot.getValue();
                if (storedValue != null) {
                    logDataSnapshot(dataSnapshot);
                    if (storedValue instanceof Map) {
                        HashMap<String, Object> map = (HashMap<String, Object>) storedValue;
                        Log.i(TAG, "Firebase Database:");
                        adapter.clear();
                        for (String player : map.keySet()) {
                            long score = (long) map.get(player);
                            adapter.addPlayerScore(player, score);
                        }
                        adapter.checkOrder();
                        adapter.notifyDataSetChanged();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        recyclerView = findViewById(R.id.high_score_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HighScoreAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void logDataSnapshot(DataSnapshot dataSnapshot) {
        Object storedValue = dataSnapshot.getValue();
        if (storedValue instanceof Map) {
            HashMap<String, Object> map = (HashMap<String, Object>) storedValue;
            Log.i(TAG, "Firebase Database:");
            printMap(0, map);
        } else {
            Log.d(TAG, String.format("%s: %s [%s]", dataSnapshot.getKey(), storedValue, storedValue.getClass().getSimpleName()));
        }
    }

    private void printMap(int depth, Map<String, Object> map) {
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof Map) {
                Log.i(TAG, "KEY: " + key);
                printMap(depth + 1, (HashMap<String, Object>) value);
            } else {
                StringBuilder indent = new StringBuilder();
                for (int i = 0; i < depth; i++) {
                    indent.append("\t");
                }
                Log.d(TAG, String.format("%s%s: %s [%s]", indent.toString(), key, value, value.getClass().getSimpleName()));
            }
        }
    }
}
