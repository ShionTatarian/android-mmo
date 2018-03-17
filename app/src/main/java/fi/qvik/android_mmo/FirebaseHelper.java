package fi.qvik.android_mmo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.Transaction.Handler;
import com.google.firebase.database.Transaction.Result;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Tommy on 08/03/2018.
 */

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";

    private final String MMO_FIREBASE = "https://android-mmo.firebaseio.com/";
    private final String FIREBASE_SCORE = "https://android-mmo.firebaseio.com/score";
    private final String FIREBASE_PLAYERS = "https://android-mmo.firebaseio.com/players";

    private static FirebaseHelper instance;
    private final Context context;

    private FirebaseDatabase database;
    private DatabaseReference scoreRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Query playersRef;

    private Set<FirebaseScoreListener> scoreListeners = new HashSet<>();
    private Set<FirebaseScoreListener> myScoreListeners = new HashSet<>();
    private Set<FirebaseHighScoreListener> highScoreListeners = new HashSet<>();

    /**
     * Get singleton instance of {@link AppUtils}.
     *
     * @return Instance of {@link AppUtils}
     */
    public static FirebaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseHelper(context);
        }

        return instance;
    }

    /**
     * Constructor.
     *
     * @param context
     */
    private FirebaseHelper(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        scoreRef = database.getReferenceFromUrl(FIREBASE_SCORE);
        Log.d(TAG, "DB[" + database.getApp().getName() + "] open: " + scoreRef.toString());

        currentUser = mAuth.getCurrentUser();
    }

    public void login(final FirebaseLoginListener loginListener) {
        if (currentUser != null) {
            loginListener.onLoginSuccess();
            return;
        }
        mAuth.signInAnonymously()
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            currentUser = mAuth.getCurrentUser();
                            loginListener.onLoginSuccess();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            loginListener.onLoginError();
                        }
                    }
                });
    }


    public FirebaseUser getUser() {
        if (currentUser == null) {
            currentUser = mAuth.getCurrentUser();
        }
        return currentUser;
    }

    public void onScoreClick() {
        String userName = AppUtils.getInstance(context).getUserName();
        if (TextUtils.isEmpty(userName)) {
            return;
        }

        DatabaseReference scoreRef = database.getReferenceFromUrl(FIREBASE_SCORE);
        scoreRef.runTransaction(new Handler() {
            @Override
            public Result doTransaction(MutableData mutableData) {
                Integer v = mutableData.getValue(Integer.class);
                if (v == null) {
                    Log.d(TAG, "doTransaction v[null]");
                    mutableData.setValue(1);
                    return Transaction.success(mutableData);
                }
                Log.d(TAG, "doTransaction oldV[" + v + "]");

                mutableData.setValue(v + 1);
                Log.d(TAG, "doTransaction newV[" + mutableData.getValue() + "]");
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });

        DatabaseReference myScoreRef = database.getReferenceFromUrl(FIREBASE_PLAYERS + "/" + userName);
        myScoreRef.runTransaction(new Handler() {
            @Override
            public Result doTransaction(MutableData mutableData) {
                Integer v = mutableData.getValue(Integer.class);
                if (v == null) {
                    Log.d(TAG, "doTransaction v[null]");
                    mutableData.setValue(1);
                    return Transaction.success(mutableData);
                }
                Log.d(TAG, "doTransaction oldV[" + v + "]");

                mutableData.setValue(v + 1);
                Log.d(TAG, "doTransaction newV[" + mutableData.getValue() + "]");
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    public void addScoreListener(FirebaseScoreListener listener) {
        scoreListeners.add(listener);
        scoreRef.addValueEventListener(firebaseScoreListener);
    }

    public void addMyScoreListener(FirebaseScoreListener listener) {
        String userName = AppUtils.getInstance(context).getUserName();
        if (TextUtils.isEmpty(userName)) {
            return;
        }
        DatabaseReference myScoreRef = database.getReferenceFromUrl(FIREBASE_PLAYERS + "/" + userName);
        myScoreListeners.add(listener);
        myScoreRef.addValueEventListener(firebaseMyScoreListener);
    }


    public void removeScoreListener(FirebaseScoreListener listener) {
        scoreListeners.remove(listener);
        if (scoreListeners.isEmpty()) {
            scoreRef.removeEventListener(firebaseScoreListener);
        }
    }

    private ValueEventListener firebaseScoreListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // This method is called once with the initial value and again
            // whenever data at this location is updated.
            Object storedValue = dataSnapshot.getValue();
            if (storedValue != null) {
                final long score = (long) storedValue;

                for (FirebaseScoreListener listener : scoreListeners) {
                    listener.onScoreChange(score);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            // Failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
        }
    };

    private ValueEventListener firebaseMyScoreListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // This method is called once with the initial value and again
            // whenever data at this location is updated.
            Object storedValue = dataSnapshot.getValue();
            if (storedValue != null) {
                final long score = (long) storedValue;

                for (FirebaseScoreListener listener : myScoreListeners) {
                    listener.onScoreChange(score);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            // Failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
        }
    };


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

    public void removeHighScoreListener(FirebaseHighScoreListener listener) {
        highScoreListeners.remove(listener);
        if (highScoreListeners.isEmpty()) {
            playersRef.removeEventListener(highScoreFirebaseListener);
        }
    }

    public void addHighScoreListener(FirebaseHighScoreListener listener) {
        highScoreListeners.add(listener);

        if (playersRef == null) {
            playersRef = database.getReferenceFromUrl(FIREBASE_PLAYERS);
        }
        playersRef.addValueEventListener(highScoreFirebaseListener);
    }

    ValueEventListener highScoreFirebaseListener = new ValueEventListener() {
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
                    List<Pair<String, Long>> list = new ArrayList<>();
                    for (String player : map.keySet()) {
                        long score = (long) map.get(player);
                        list.add(new Pair(player, score));
                    }

                    for (FirebaseHighScoreListener listener : highScoreListeners) {
                        listener.onHighScoreUpdated(list);
                    }

                }

            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            // Failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
        }
    };

    public interface FirebaseLoginListener {

        void onLoginSuccess();

        void onLoginError();

        // TODO: logout?

    }

    public interface FirebaseScoreListener {

        void onScoreChange(long score);

    }

    public interface FirebaseHighScoreListener {

        void onHighScoreUpdated(List<Pair<String, Long>> highScores);

    }


}
