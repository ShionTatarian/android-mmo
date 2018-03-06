package fi.qvik.android_mmo;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fi.qvik.android_mmo.HighScoreAdapter.HighScoreVH;

/**
 * Created by Tommy on 06/03/2018.
 */

public class HighScoreAdapter extends RecyclerView.Adapter<HighScoreVH> {

    private List<Pair<String, Long>> list = new ArrayList<>();

    @NonNull
    @Override
    public HighScoreVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HighScoreVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.high_score_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HighScoreVH holder, int position) {
        Pair<String, Long> item = list.get(position);
        holder.playerText.setText(item.first);
        holder.scoreText.setText(String.valueOf(item.second));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void addPlayerScore(String player, long score) {
        list.add(new Pair<>(player, score));
    }

    public void clear() {
        list.clear();
    }

    private Comparator<Pair<String, Long>> highScoreComparator = new Comparator<Pair<String, Long>>() {
        @Override
        public int compare(Pair<String, Long> p1, Pair<String, Long> p2) {
            return p1.second > p2.second ? -1 :
                    p1.second < p2.second ? 1 : 0;
        }
    };

    public void checkOrder() {
        // ordering using Firebase would require different data with named score field for each player
        Collections.sort(list, highScoreComparator);
    }

    class HighScoreVH extends ViewHolder {

        private TextView playerText;
        private TextView scoreText;

        public HighScoreVH(View v) {
            super(v);
            playerText = v.findViewById(R.id.high_score_row_player_text);
            scoreText = v.findViewById(R.id.high_score_row_score_text);
        }
    }

}
