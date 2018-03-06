package fi.qvik.android_mmo;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.Transaction;
import com.google.firebase.database.Transaction.Handler;
import com.google.firebase.database.Transaction.Result;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private final String MY_FIREBASE = "https://android-mmo.firebaseio.com/";
    private final String FIREBASE_SCORE = "https://android-mmo.firebaseio.com/score";
    private final String FIREBASE_PLAYERS = "https://android-mmo.firebaseio.com/players";
    private final String USER_NAME_KEY = "user_name";
    private FirebaseDatabase database;
    private DatabaseReference scoreRef;

    private TextView scoreText;
    private TextView errorText;
    private Button button;
    private Button loginButton;
    private EditText userNameEdit;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private AppUtils appUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appUtils = AppUtils.getInstance(this);
        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        scoreRef = database.getReferenceFromUrl(FIREBASE_SCORE);
        Log.d(TAG, "DB[" + database.getApp().getName() + "] open: " + scoreRef.toString());
        addScoreListener();

        scoreText = findViewById(R.id.score_text);
        errorText = findViewById(R.id.error_text);
        button = findViewById(R.id.button);
        loginButton = findViewById(R.id.login_button);
        userNameEdit = findViewById(R.id.user_name_edit);
        String userName = appUtils.loadString(USER_NAME_KEY, "");
        userNameEdit.setText(userName);

        userNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence)) {
                    errorText.setVisibility(View.INVISIBLE);

                    appUtils.storeString(USER_NAME_KEY, charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        updateScore(0);
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            onLoginClick(null);
        }
        updateContent();
    }

    private void updateContent() {
        loginButton.setVisibility(currentUser != null ? View.GONE : View.VISIBLE);
        button.setVisibility(currentUser != null ? View.VISIBLE : View.GONE);
    }

    private void addScoreListener() {
        scoreRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Object storedValue = dataSnapshot.getValue();
                if (storedValue != null) {
                    final long count = (long) storedValue;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateScore(count);
                        }
                    });
                    logDataSnapshot(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
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

    private void updateScore(long count) {
        scoreText.setText(getString(R.string.score, count));
    }

    public void onButtonClick(View view) {
        String userName = userNameEdit.getText().toString();
        if (TextUtils.isEmpty(userName)) {
            errorText.setText("Username required!");
            errorText.setVisibility(View.VISIBLE);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onLoginClick(View view) {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            currentUser = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        updateContent();
                    }
                });
    }

    public void onHighScoreClick(View view) {
        Intent intent = new Intent(this, HighScoreActivity.class);
        startActivity(intent);
    }
}
