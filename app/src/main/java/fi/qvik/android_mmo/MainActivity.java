package fi.qvik.android_mmo;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.support.annotation.ColorInt;
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
import android.widget.ImageButton;
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

import fi.qvik.android_mmo.FirebaseHelper.FirebaseLoginListener;
import fi.qvik.android_mmo.FirebaseHelper.FirebaseScoreListener;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private TextView scoreText;
    private TextView errorText;
    private ImageButton button;
    private Button loginButton;
    private EditText userNameEdit;

    private AppUtils appUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appUtils = AppUtils.getInstance(this);

        scoreText = findViewById(R.id.score_text);
        errorText = findViewById(R.id.error_text);
        button = findViewById(R.id.button);
        loginButton = findViewById(R.id.login_button);
        userNameEdit = findViewById(R.id.user_name_edit);

        String userName = appUtils.getUserName();
        userNameEdit.setText(userName);
        if (!TextUtils.isEmpty(userName)) {
            userNameEdit.setSelection(userName.length()); // set write cursor position to end
        }
        userNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence)) {
                    errorText.setVisibility(View.INVISIBLE);
                    appUtils.setUserName(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        updateScore("?");
        FirebaseHelper.getInstance(this).addScoreListener(scoreListener);
        FirebaseHelper.getInstance(this).login(loginListener);
    }

    private void updateContent() {
        FirebaseUser user = FirebaseHelper.getInstance(this).getUser();

        loginButton.setVisibility(user != null ? View.GONE : View.VISIBLE);
        button.setVisibility(user != null ? View.VISIBLE : View.GONE);
    }

    private FirebaseLoginListener loginListener = new FirebaseLoginListener() {
        @Override
        public void onLoginSuccess() {
            updateContent();
        }

        @Override
        public void onLoginError() {
            // TODO: show error?
        }
    };

    private FirebaseScoreListener scoreListener = new FirebaseScoreListener() {
        @Override
        public void onScoreChange(long score) {
            updateScore(getString(R.string.score, score));
        }
    };

    private void updateScore(String score) {
        scoreText.setText(score);
    }

    public void onButtonClick(View view) {
        String userName = userNameEdit.getText().toString();
        if (TextUtils.isEmpty(userName)) {
            errorText.setText("Username required!");
            errorText.setVisibility(View.VISIBLE);
            return;
        }

        FirebaseHelper.getInstance(this).onScoreClick();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseHelper.getInstance(this).removeScoreListener(scoreListener);
    }

    public void onLoginClick(View view) {
        FirebaseHelper.getInstance(this).login(loginListener);
    }

    public void onHighScoreClick(View view) {
        Intent intent = new Intent(this, HighScoreActivity.class);
        startActivity(intent);
    }

    public void onIQuitClick(View view) {
        finish();

    }

}
