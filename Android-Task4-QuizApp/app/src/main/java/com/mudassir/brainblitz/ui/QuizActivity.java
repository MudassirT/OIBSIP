package com.mudassir.brainblitz.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.mudassir.brainblitz.R;
import com.mudassir.brainblitz.data.Question;
import com.mudassir.brainblitz.data.QuestionRepository;
import com.mudassir.brainblitz.data.QuestionReview;
import com.mudassir.brainblitz.logic.QuizManager;
import com.mudassir.brainblitz.util.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Quiz activity managing question display, option interaction, live timer countdown,
 * accessibility-friendly feedback, and navigation to the results screen.
 */
public class QuizActivity extends AppCompatActivity {

    private QuizViewModel viewModel;
    private QuizManager quizManager;

    // UI Elements
    private TextView tvQuestionCounter;
    private TextView tvLiveScore;
    private LinearProgressIndicator progressQuiz;
    private TextView tvTimer;
    private ImageView ivTimerIcon;
    private TextView tvCategoryBadge;
    private TextView tvQuestionText;
    private ScrollView scrollQuiz;

    private MaterialCardView[] optionCards;
    private TextView[] optionTexts;
    private ImageView[] optionIcons;

    private MaterialCardView cardExplanation;
    private TextView tvFeedbackStatus;
    private TextView tvExplanationText;
    private MaterialButton btnNext;

    // Timer
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        viewModel = new ViewModelProvider(this).get(QuizViewModel.class);
        quizManager = viewModel.getQuizManager();

        initViews();
        setupBackPressHandler();

        if (!viewModel.isRoundInitialized()) {
            QuestionRepository repository = new QuestionRepository();
            List<Question> pool = repository.loadQuestions(this);
            viewModel.startNewRound(pool, Constants.QUESTIONS_PER_ROUND);
        }

        displayCurrentQuestion();
        setupListeners();
    }

    private void initViews() {
        tvQuestionCounter = findViewById(R.id.tvQuestionCounter);
        tvLiveScore = findViewById(R.id.tvLiveScore);
        progressQuiz = findViewById(R.id.progressQuiz);
        tvTimer = findViewById(R.id.tvTimer);
        ivTimerIcon = findViewById(R.id.ivTimerIcon);
        tvCategoryBadge = findViewById(R.id.tvCategoryBadge);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        scrollQuiz = findViewById(R.id.scrollQuiz);

        optionCards = new MaterialCardView[]{
                findViewById(R.id.cardOptionA),
                findViewById(R.id.cardOptionB),
                findViewById(R.id.cardOptionC),
                findViewById(R.id.cardOptionD)
        };

        optionTexts = new TextView[]{
                findViewById(R.id.tvTextOptionA),
                findViewById(R.id.tvTextOptionB),
                findViewById(R.id.tvTextOptionC),
                findViewById(R.id.tvTextOptionD)
        };

        optionIcons = new ImageView[]{
                findViewById(R.id.ivIconOptionA),
                findViewById(R.id.ivIconOptionB),
                findViewById(R.id.ivIconOptionC),
                findViewById(R.id.ivIconOptionD)
        };

        cardExplanation = findViewById(R.id.cardExplanation);
        tvFeedbackStatus = findViewById(R.id.tvFeedbackStatus);
        tvExplanationText = findViewById(R.id.tvExplanationText);
        btnNext = findViewById(R.id.btnNext);
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showQuitConfirmationDialog();
            }
        });
    }

    private void showQuitConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_quit_title)
                .setMessage(R.string.dialog_quit_message)
                .setPositiveButton(R.string.dialog_quit_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.dialog_quit_cancel, null)
                .show();
    }

    private void setupListeners() {
        for (int i = 0; i < optionCards.length; i++) {
            final int optionIndex = i;
            optionCards[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleOptionSelected(optionIndex);
                }
            });
        }

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNextClicked();
            }
        });
    }

    private void displayCurrentQuestion() {
        Question question = quizManager.getCurrentQuestion();
        if (question == null) {
            navigateToResults();
            return;
        }

        // Header statistics
        int currentNumber = quizManager.getCurrentQuestionNumber();
        int totalQuestions = quizManager.getTotalQuestions();
        tvQuestionCounter.setText(getString(R.string.quiz_question_counter, currentNumber, totalQuestions));
        tvLiveScore.setText(getString(R.string.quiz_live_score, quizManager.getScore()));

        progressQuiz.setMax(totalQuestions);
        progressQuiz.setProgress(currentNumber);

        tvCategoryBadge.setText(question.getCategory());
        tvQuestionText.setText(question.getQuestionText());

        // Reset and populate options
        List<String> options = question.getOptions();
        for (int i = 0; i < optionCards.length; i++) {
            if (i < options.size()) {
                optionCards[i].setVisibility(View.VISIBLE);
                optionTexts[i].setText(options.get(i));
                resetOptionCardStyle(i);
            } else {
                optionCards[i].setVisibility(View.GONE);
            }
        }

        // Update Next button label
        if (quizManager.isLastQuestion()) {
            btnNext.setText(R.string.quiz_btn_see_results);
        } else {
            btnNext.setText(R.string.quiz_btn_next);
        }

        // If this question was already answered before rotation/recreation
        if (quizManager.isCurrentAnswered()) {
            renderAnsweredState(quizManager.getSelectedOptionIndex(), question.getCorrectOptionIndex());
        } else {
            cardExplanation.setVisibility(View.GONE);
            btnNext.setEnabled(false);
            startTimer(viewModel.getRemainingTimeMillis());
        }

        // Scroll back to top on each new question
        scrollQuiz.smoothScrollTo(0, 0);
    }

    private void resetOptionCardStyle(int index) {
        MaterialCardView card = optionCards[index];
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.option_bg_normal));
        card.setStrokeColor(ContextCompat.getColor(this, R.color.option_stroke_normal));
        card.setStrokeWidth((int) (1.5f * getResources().getDisplayMetrics().density));
        card.setClickable(true);
        card.setFocusable(true);

        optionTexts[index].setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        optionIcons[index].setVisibility(View.GONE);
    }

    private void handleOptionSelected(int selectedIndex) {
        // Prevent rapid double-taps
        if (quizManager.isCurrentAnswered()) {
            return;
        }

        cancelTimer();

        Question question = quizManager.getCurrentQuestion();
        if (question == null) {
            return;
        }

        boolean isCorrect = quizManager.submitAnswer(selectedIndex);
        renderAnsweredState(selectedIndex, question.getCorrectOptionIndex());

        // Update live score
        tvLiveScore.setText(getString(R.string.quiz_live_score, quizManager.getScore()));
    }

    private void handleTimerExpired() {
        if (quizManager.isCurrentAnswered()) {
            return;
        }

        Question question = quizManager.getCurrentQuestion();
        if (question == null) {
            return;
        }

        quizManager.submitTimeout();
        renderAnsweredState(-1, question.getCorrectOptionIndex());
    }

    private void renderAnsweredState(int selectedIndex, int correctIndex) {
        cancelTimer();

        // Lock all options
        for (MaterialCardView card : optionCards) {
            card.setClickable(false);
            card.setFocusable(false);
        }

        // Highlight correct option
        if (correctIndex >= 0 && correctIndex < optionCards.length) {
            MaterialCardView correctCard = optionCards[correctIndex];
            correctCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.feedback_correct_bg));
            correctCard.setStrokeColor(ContextCompat.getColor(this, R.color.feedback_correct_stroke));
            correctCard.setStrokeWidth((int) (2 * getResources().getDisplayMetrics().density));
            optionTexts[correctIndex].setTextColor(ContextCompat.getColor(this, R.color.feedback_correct));

            optionIcons[correctIndex].setVisibility(View.VISIBLE);
            optionIcons[correctIndex].setImageResource(R.drawable.ic_check_circle);
            optionIcons[correctIndex].setColorFilter(ContextCompat.getColor(this, R.color.feedback_correct));
        }

        // If user made a wrong choice
        if (selectedIndex != -1 && selectedIndex != correctIndex) {
            MaterialCardView wrongCard = optionCards[selectedIndex];
            wrongCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.feedback_wrong_bg));
            wrongCard.setStrokeColor(ContextCompat.getColor(this, R.color.feedback_wrong_stroke));
            wrongCard.setStrokeWidth((int) (2 * getResources().getDisplayMetrics().density));
            optionTexts[selectedIndex].setTextColor(ContextCompat.getColor(this, R.color.feedback_wrong));

            optionIcons[selectedIndex].setVisibility(View.VISIBLE);
            optionIcons[selectedIndex].setImageResource(R.drawable.ic_close_circle);
            optionIcons[selectedIndex].setColorFilter(ContextCompat.getColor(this, R.color.feedback_wrong));
        }

        // Explanation text & feedback card
        Question question = quizManager.getCurrentQuestion();
        cardExplanation.setVisibility(View.VISIBLE);

        if (selectedIndex == correctIndex) {
            tvFeedbackStatus.setText(R.string.quiz_feedback_correct);
            tvFeedbackStatus.setTextColor(ContextCompat.getColor(this, R.color.feedback_correct));
        } else if (selectedIndex == -1) {
            tvFeedbackStatus.setText(R.string.quiz_feedback_timeout);
            tvFeedbackStatus.setTextColor(ContextCompat.getColor(this, R.color.feedback_timed_out));
        } else {
            tvFeedbackStatus.setText(R.string.quiz_feedback_wrong);
            tvFeedbackStatus.setTextColor(ContextCompat.getColor(this, R.color.feedback_wrong));
        }

        if (question != null && !question.getExplanation().isEmpty()) {
            tvExplanationText.setText(question.getExplanation());
            tvExplanationText.setVisibility(View.VISIBLE);
        } else {
            tvExplanationText.setVisibility(View.GONE);
        }

        btnNext.setEnabled(true);
    }

    private void handleNextClicked() {
        if (quizManager.isLastQuestion()) {
            navigateToResults();
        } else {
            // Apply subtle fade transition
            AlphaAnimation fade = new AlphaAnimation(0.2f, 1.0f);
            fade.setDuration(300);
            scrollQuiz.startAnimation(fade);

            quizManager.moveToNextQuestion();
            viewModel.resetTimer();
            displayCurrentQuestion();
        }
    }

    private void navigateToResults() {
        cancelTimer();

        Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
        intent.putExtra(Constants.EXTRA_SCORE, quizManager.getScore());
        intent.putExtra(Constants.EXTRA_TOTAL_QUESTIONS, quizManager.getTotalQuestions());
        intent.putExtra(Constants.EXTRA_CORRECT_COUNT, quizManager.getCorrectCount());
        intent.putExtra(Constants.EXTRA_INCORRECT_COUNT, quizManager.getIncorrectCount());
        intent.putExtra(Constants.EXTRA_TIMED_OUT_COUNT, quizManager.getTimedOutCount());
        intent.putExtra(Constants.EXTRA_REVIEW_LIST, (Serializable) new ArrayList<>(quizManager.getReviewList()));

        startActivity(intent);
        finish(); // Finish QuizActivity so Back from Results goes to Welcome
    }

    private void startTimer(long durationMillis) {
        cancelTimer();

        countDownTimer = new CountDownTimer(durationMillis, Constants.TIMER_INTERVAL_MILLIS) {
            @Override
            public void onTick(long millisUntilFinished) {
                viewModel.setRemainingTimeMillis(millisUntilFinished);
                int seconds = (int) (millisUntilFinished / 1000L);
                tvTimer.setText(getString(R.string.quiz_timer_format, seconds));

                if (seconds <= 5) {
                    tvTimer.setTextColor(ContextCompat.getColor(QuizActivity.this, R.color.timer_critical));
                    ivTimerIcon.setColorFilter(ContextCompat.getColor(QuizActivity.this, R.color.timer_critical));
                } else if (seconds <= 10) {
                    tvTimer.setTextColor(ContextCompat.getColor(QuizActivity.this, R.color.timer_warning));
                    ivTimerIcon.setColorFilter(ContextCompat.getColor(QuizActivity.this, R.color.timer_warning));
                } else {
                    tvTimer.setTextColor(ContextCompat.getColor(QuizActivity.this, R.color.timer_normal));
                    ivTimerIcon.setColorFilter(ContextCompat.getColor(QuizActivity.this, R.color.timer_normal));
                }
            }

            @Override
            public void onFinish() {
                viewModel.setRemainingTimeMillis(0);
                tvTimer.setText(getString(R.string.quiz_timer_format, 0));
                handleTimerExpired();
            }
        }.start();

        viewModel.setTimerActive(true);
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        viewModel.setTimerActive(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
    }
}
