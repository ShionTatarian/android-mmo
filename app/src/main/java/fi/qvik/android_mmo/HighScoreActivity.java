package fi.qvik.android_mmo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;

import java.util.List;

import fi.qvik.android_mmo.FirebaseHelper.FirebaseHighScoreListener;

/**
 * Created by Tommy on 06/03/2018.
 */

public class HighScoreActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private RecyclerView recyclerView;
    private HighScoreAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.high_score_activity);
        FirebaseHelper.getInstance(this).addHighScoreListener(highScoreListener);

        recyclerView = findViewById(R.id.high_score_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HighScoreAdapter();
        recyclerView.setAdapter(adapter);
    }

    private FirebaseHighScoreListener highScoreListener = new FirebaseHighScoreListener() {
        @Override
        public void onHighScoreUpdated(List<Pair<String, Long>> highScores) {
            adapter.clear();
            for (Pair<String, Long> pair : highScores) {
                adapter.addPlayerScore(pair);
            }
            adapter.checkOrder();
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseHelper.getInstance(this).removeHighScoreListener(highScoreListener);
    }
}
