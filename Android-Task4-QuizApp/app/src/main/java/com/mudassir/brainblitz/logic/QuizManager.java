package com.mudassir.brainblitz.logic;

import com.mudassir.brainblitz.data.Question;
import com.mudassir.brainblitz.data.QuestionReview;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Core business logic for managing a quiz session, including question selection,
 * option shuffling, answer verification, and score computation.
 * Kept as a pure Java class for effortless unit testing.
 */
public class QuizManager implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum PerformanceRating {
        OUTSTANDING,
        GREAT,
        GOOD,
        KEEP_PRACTICING
    }

    private final List<Question> roundQuestions = new ArrayList<>();
    private final List<QuestionReview> reviewList = new ArrayList<>();

    private int currentIndex = 0;
    private int correctCount = 0;
    private int incorrectCount = 0;
    private int timedOutCount = 0;
    private boolean isCurrentAnswered = false;
    private int selectedOptionIndex = -1;

    public QuizManager() {
    }

    /**
     * Initializes a new quiz round from a master question pool.
     * Selects up to roundSize random questions, shuffles their order,
     * and shuffles each question's 4 options with index remapping.
     *
     * @param masterPool The available questions pool.
     * @param roundSize  The desired number of questions in this round.
     */
    public void initRound(List<Question> masterPool, int roundSize) {
        roundQuestions.clear();
        reviewList.clear();
        currentIndex = 0;
        correctCount = 0;
        incorrectCount = 0;
        timedOutCount = 0;
        isCurrentAnswered = false;
        selectedOptionIndex = -1;

        if (masterPool == null || masterPool.isEmpty()) {
            return;
        }

        // Shuffle a copy of the pool to randomize question order
        List<Question> poolCopy = new ArrayList<>(masterPool);
        Collections.shuffle(poolCopy);

        int countToPick = Math.min(roundSize, poolCopy.size());
        for (int i = 0; i < countToPick; i++) {
            Question original = poolCopy.get(i);
            // Shuffle the 4 options of each selected question
            roundQuestions.add(original.createShuffledCopy());
        }
    }

    public List<Question> getRoundQuestions() {
        return Collections.unmodifiableList(roundQuestions);
    }

    public Question getCurrentQuestion() {
        if (currentIndex >= 0 && currentIndex < roundQuestions.size()) {
            return roundQuestions.get(currentIndex);
        }
        return null;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getCurrentQuestionNumber() {
        return currentIndex + 1;
    }

    public int getTotalQuestions() {
        return roundQuestions.size();
    }

    public boolean isCurrentAnswered() {
        return isCurrentAnswered;
    }

    public int getSelectedOptionIndex() {
        return selectedOptionIndex;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getIncorrectCount() {
        return incorrectCount;
    }

    public int getTimedOutCount() {
        return timedOutCount;
    }

    public int getScore() {
        return correctCount;
    }

    public boolean isLastQuestion() {
        return !roundQuestions.isEmpty() && currentIndex == roundQuestions.size() - 1;
    }

    public boolean isRoundOver() {
        return roundQuestions.isEmpty() || currentIndex >= roundQuestions.size();
    }

    /**
     * Submits an answer for the current question.
     *
     * @param optionIndex The index (0..3) chosen by the user.
     * @return true if the answer was correct, false otherwise.
     */
    public boolean submitAnswer(int optionIndex) {
        if (isCurrentAnswered || isRoundOver()) {
            return false;
        }

        Question current = getCurrentQuestion();
        if (current == null) {
            return false;
        }

        isCurrentAnswered = true;
        selectedOptionIndex = optionIndex;

        boolean isCorrect = (optionIndex == current.getCorrectOptionIndex());
        if (isCorrect) {
            correctCount++;
        } else {
            incorrectCount++;
        }

        reviewList.add(new QuestionReview(
                current.getQuestionText(),
                current.getCategory(),
                current.getOptions(),
                optionIndex,
                current.getCorrectOptionIndex(),
                isCorrect,
                false,
                current.getExplanation()
        ));

        return isCorrect;
    }

    /**
     * Submits a timeout when the countdown expires.
     */
    public void submitTimeout() {
        if (isCurrentAnswered || isRoundOver()) {
            return;
        }

        Question current = getCurrentQuestion();
        if (current == null) {
            return;
        }

        isCurrentAnswered = true;
        selectedOptionIndex = -1;
        incorrectCount++;
        timedOutCount++;

        reviewList.add(new QuestionReview(
                current.getQuestionText(),
                current.getCategory(),
                current.getOptions(),
                -1,
                current.getCorrectOptionIndex(),
                false,
                true,
                current.getExplanation()
        ));
    }

    /**
     * Moves to the next question in the round.
     *
     * @return true if advanced to a valid next question, false if the round ended.
     */
    public boolean moveToNextQuestion() {
        if (currentIndex < roundQuestions.size() - 1) {
            currentIndex++;
            isCurrentAnswered = false;
            selectedOptionIndex = -1;
            return true;
        }
        return false;
    }

    /**
     * Calculates the score percentage (0..100).
     */
    public int calculatePercentage() {
        if (roundQuestions.isEmpty()) {
            return 0;
        }
        return (int) Math.round(((double) correctCount / roundQuestions.size()) * 100);
    }

    /**
     * Computes a performance tier based on the user's score percentage.
     */
    public PerformanceRating getPerformanceRating() {
        int percentage = calculatePercentage();
        if (percentage >= 90) {
            return PerformanceRating.OUTSTANDING;
        } else if (percentage >= 70) {
            return PerformanceRating.GREAT;
        } else if (percentage >= 50) {
            return PerformanceRating.GOOD;
        } else {
            return PerformanceRating.KEEP_PRACTICING;
        }
    }

    public List<QuestionReview> getReviewList() {
        return Collections.unmodifiableList(reviewList);
    }

    // State restoration methods for configuration changes / ViewModel
    public void restoreState(List<Question> questions, List<QuestionReview> reviews,
                             int index, int correct, int incorrect, int timedOut,
                             boolean answered, int selectedIndex) {
        this.roundQuestions.clear();
        if (questions != null) {
            this.roundQuestions.addAll(questions);
        }
        this.reviewList.clear();
        if (reviews != null) {
            this.reviewList.addAll(reviews);
        }
        this.currentIndex = index;
        this.correctCount = correct;
        this.incorrectCount = incorrect;
        this.timedOutCount = timedOut;
        this.isCurrentAnswered = answered;
        this.selectedOptionIndex = selectedIndex;
    }
}
