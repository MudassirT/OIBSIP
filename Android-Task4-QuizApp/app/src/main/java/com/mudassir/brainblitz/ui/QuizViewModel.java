package com.mudassir.brainblitz.ui;

import androidx.lifecycle.ViewModel;

import com.mudassir.brainblitz.data.Question;
import com.mudassir.brainblitz.logic.QuizManager;
import com.mudassir.brainblitz.util.Constants;

import java.util.List;

/**
 * ViewModel that maintains the QuizManager and timer state across Activity lifecycle events
 * such as screen orientation changes.
 */
public class QuizViewModel extends ViewModel {

    private final QuizManager quizManager = new QuizManager();
    private boolean isRoundInitialized = false;
    private long remainingTimeMillis = Constants.QUESTION_TIMER_SECONDS * 1000L;
    private boolean isTimerActive = false;

    public QuizManager getQuizManager() {
        return quizManager;
    }

    public boolean isRoundInitialized() {
        return isRoundInitialized;
    }

    public void startNewRound(List<Question> pool, int roundSize) {
        quizManager.initRound(pool, roundSize);
        isRoundInitialized = true;
        resetTimer();
    }

    public long getRemainingTimeMillis() {
        return remainingTimeMillis;
    }

    public void setRemainingTimeMillis(long remainingTimeMillis) {
        this.remainingTimeMillis = remainingTimeMillis;
    }

    public boolean isTimerActive() {
        return isTimerActive;
    }

    public void setTimerActive(boolean timerActive) {
        isTimerActive = timerActive;
    }

    public void resetTimer() {
        this.remainingTimeMillis = Constants.QUESTION_TIMER_SECONDS * 1000L;
        this.isTimerActive = false;
    }
}
