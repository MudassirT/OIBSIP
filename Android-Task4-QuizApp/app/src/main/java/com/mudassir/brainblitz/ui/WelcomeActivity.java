package com.mudassir.brainblitz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.mudassir.brainblitz.R;
import com.mudassir.brainblitz.util.Constants;
import com.mudassir.brainblitz.util.ScoreManager;

/**
 * Launcher activity welcoming the user, displaying personal best statistics,
 * and initiating a new quiz round.
 */
public class WelcomeActivity extends AppCompatActivity {

    private TextView tvBestScoreValue;
    private MaterialButton btnStartQuiz;
    private ScoreManager scoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        scoreManager = new ScoreManager(this);
        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBestScoreDisplay();
    }

    private void initViews() {
        tvBestScoreValue = findViewById(R.id.tvBestScoreValue);
        btnStartQuiz = findViewById(R.id.btnStartQuiz);
    }

    private void setupListeners() {
        btnStartQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, QuizActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateBestScoreDisplay() {
        int bestScore = scoreManager.getBestScore();
        if (bestScore >= 0) {
            int percentage = scoreManager.getBestPercentage();
            tvBestScoreValue.setText(getString(R.string.welcome_best_score_format,
                    bestScore, Constants.QUESTIONS_PER_ROUND, percentage));
        } else {
            tvBestScoreValue.setText(getString(R.string.welcome_no_score_yet));
        }
    }
}
