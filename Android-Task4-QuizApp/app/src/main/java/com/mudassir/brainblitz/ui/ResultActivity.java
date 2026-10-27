package com.mudassir.brainblitz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mudassir.brainblitz.R;
import com.mudassir.brainblitz.data.QuestionReview;
import com.mudassir.brainblitz.util.Constants;
import com.mudassir.brainblitz.util.ScoreManager;

import java.util.ArrayList;
import java.util.List;

/**
 * ResultActivity displaying total score, percentage accuracy, performance tier feedback,
 * high score achievements, question review dialog, and navigation options.
 */
public class ResultActivity extends AppCompatActivity {

    private TextView tvScorePercentage;
    private TextView tvScoreFraction;
    private TextView tvPerformanceMessage;
    private TextView tvCorrectStat;
    private TextView tvIncorrectStat;
    private TextView tvTimedOutStat;
    private TextView tvNewHighScoreBadge;
    private TextView tvBestScoreResultText;

    private MaterialButton btnReviewAnswers;
    private MaterialButton btnRestartQuiz;
    private MaterialButton btnHome;

    private ScoreManager scoreManager;
    private List<QuestionReview> reviewList = new ArrayList<>();

    private int score = 0;
    private int totalQuestions = 0;
    private int correctCount = 0;
    private int incorrectCount = 0;
    private int timedOutCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        scoreManager = new ScoreManager(this);

        extractIntentData();
        initViews();
        displayResults();
        setupListeners();
        setupBackPressHandler();
    }

    @SuppressWarnings("unchecked")
    private void extractIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            score = intent.getIntExtra(Constants.EXTRA_SCORE, 0);
            totalQuestions = intent.getIntExtra(Constants.EXTRA_TOTAL_QUESTIONS, Constants.QUESTIONS_PER_ROUND);
            correctCount = intent.getIntExtra(Constants.EXTRA_CORRECT_COUNT, 0);
            incorrectCount = intent.getIntExtra(Constants.EXTRA_INCORRECT_COUNT, 0);
            timedOutCount = intent.getIntExtra(Constants.EXTRA_TIMED_OUT_COUNT, 0);

            List<QuestionReview> passedReviews = (List<QuestionReview>) intent.getSerializableExtra(Constants.EXTRA_REVIEW_LIST);
            if (passedReviews != null) {
                reviewList = passedReviews;
            }
        }
    }

    private void initViews() {
        tvScorePercentage = findViewById(R.id.tvScorePercentage);
        tvScoreFraction = findViewById(R.id.tvScoreFraction);
        tvPerformanceMessage = findViewById(R.id.tvPerformanceMessage);
        tvCorrectStat = findViewById(R.id.tvCorrectStat);
        tvIncorrectStat = findViewById(R.id.tvIncorrectStat);
        tvTimedOutStat = findViewById(R.id.tvTimedOutStat);
        tvNewHighScoreBadge = findViewById(R.id.tvNewHighScoreBadge);
        tvBestScoreResultText = findViewById(R.id.tvBestScoreResultText);

        btnReviewAnswers = findViewById(R.id.btnReviewAnswers);
        btnRestartQuiz = findViewById(R.id.btnRestartQuiz);
        btnHome = findViewById(R.id.btnHome);
    }

    private void displayResults() {
        // Record score and check for new high score
        boolean isNewHigh = scoreManager.recordQuizResult(score, totalQuestions);
        if (isNewHigh) {
            tvNewHighScoreBadge.setVisibility(View.VISIBLE);
        } else {
            tvNewHighScoreBadge.setVisibility(View.GONE);
        }

        // Percentage computation
        int percentage = totalQuestions > 0 ? (int) Math.round(((double) score / totalQuestions) * 100) : 0;
        tvScorePercentage.setText(getString(R.string.results_percentage_format, percentage));
        tvScoreFraction.setText(getString(R.string.results_score_format, score, totalQuestions));

        // Feedback tier
        if (percentage >= 90) {
            tvPerformanceMessage.setText(R.string.rating_outstanding);
        } else if (percentage >= 70) {
            tvPerformanceMessage.setText(R.string.rating_great);
        } else if (percentage >= 50) {
            tvPerformanceMessage.setText(R.string.rating_good);
        } else {
            tvPerformanceMessage.setText(R.string.rating_keep_practicing);
        }

        // Stats breakdown
        tvCorrectStat.setText(String.valueOf(correctCount));
        tvIncorrectStat.setText(String.valueOf(incorrectCount));
        tvTimedOutStat.setText(String.valueOf(timedOutCount));

        // Best score info
        int bestScore = scoreManager.getBestScore();
        int bestPercentage = scoreManager.getBestPercentage();
        tvBestScoreResultText.setText(getString(R.string.results_best_score_format, bestScore, totalQuestions, bestPercentage));

        // Review button text
        btnReviewAnswers.setText(getString(R.string.results_btn_review, reviewList.size()));
        btnReviewAnswers.setEnabled(!reviewList.isEmpty());
    }

    private void setupListeners() {
        btnRestartQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this, QuizActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToHome();
            }
        });

        btnReviewAnswers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReviewDialog();
            }
        });
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToHome();
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(ResultActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void showReviewDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_review, null);
        LinearLayout container = dialogView.findViewById(R.id.reviewItemsContainer);
        MaterialButton btnClose = dialogView.findViewById(R.id.btnCloseReview);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < reviewList.size(); i++) {
            QuestionReview review = reviewList.get(i);
            View itemView = inflater.inflate(R.layout.item_question_review, container, false);

            TextView tvCategory = itemView.findViewById(R.id.tvReviewCategory);
            TextView tvStatus = itemView.findViewById(R.id.tvReviewStatus);
            TextView tvQuestion = itemView.findViewById(R.id.tvReviewQuestion);
            TextView tvUserAnswer = itemView.findViewById(R.id.tvReviewUserAnswer);
            TextView tvCorrectAnswer = itemView.findViewById(R.id.tvReviewCorrectAnswer);
            TextView tvExplanation = itemView.findViewById(R.id.tvReviewExplanation);

            tvCategory.setText(String.format("Q%d • %s", (i + 1), review.getCategory()));
            tvQuestion.setText(review.getQuestionText());
            tvCorrectAnswer.setText(getString(R.string.review_correct_answer, review.getCorrectOptionText()));

            if (review.isCorrect()) {
                tvStatus.setText(R.string.review_status_correct);
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.feedback_correct));
                tvStatus.setBackgroundResource(R.drawable.bg_badge_rounded);
                tvUserAnswer.setText(getString(R.string.review_your_answer, review.getSelectedOptionText()));
                tvUserAnswer.setTextColor(ContextCompat.getColor(this, R.color.feedback_correct));
            } else if (review.isTimedOut()) {
                tvStatus.setText(R.string.review_status_timeout);
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.feedback_timed_out));
                tvStatus.setBackgroundResource(R.drawable.bg_badge_rounded);
                tvUserAnswer.setText(getString(R.string.review_your_answer, getString(R.string.review_status_timeout)));
                tvUserAnswer.setTextColor(ContextCompat.getColor(this, R.color.feedback_timed_out));
            } else {
                tvStatus.setText(R.string.review_status_incorrect);
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.feedback_wrong));
                tvStatus.setBackgroundResource(R.drawable.bg_badge_rounded);
                tvUserAnswer.setText(getString(R.string.review_your_answer, review.getSelectedOptionText()));
                tvUserAnswer.setTextColor(ContextCompat.getColor(this, R.color.feedback_wrong));
            }

            if (review.getExplanation() != null && !review.getExplanation().isEmpty()) {
                tvExplanation.setText(review.getExplanation());
                tvExplanation.setVisibility(View.VISIBLE);
            } else {
                tvExplanation.setVisibility(View.GONE);
            }

            container.addView(itemView);
        }

        dialog.show();
    }
}
