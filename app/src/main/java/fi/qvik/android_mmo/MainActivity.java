package fi.qvik.android_mmo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private int count = 0;
    private TextView scoreText;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scoreText = findViewById(R.id.score_text);
        button = findViewById(R.id.button);

        updateScore();
    }

    private void updateScore() {
        scoreText.setText(getString(R.string.score, count));
    }

    public void onButtonClick(View view) {
        count += 1;
        updateScore();
    }
}
