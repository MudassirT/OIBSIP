package com.mudassir.examportal.service;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.model.ExamResult;
import com.mudassir.examportal.model.ExamStatus;
import com.mudassir.examportal.model.Question;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Plain Java class encapsulating all state and business logic for an active exam session.
 * Completely decoupled from Swing UI and accepts an injectable time supplier for deterministic unit testing.
 * All submissions are strictly idempotent.
 */
public class ExamSession {

    private final List<Question> questions;
    private final Duration totalDuration;
    private final Supplier<Instant> clock;
    private final Instant startTime;

    private int currentIndex = 0;
    private final Map<Integer, String> userAnswers = new HashMap<>(); // question index (0-based) -> selected option letter ("A", "B", "C", "D")
    private final Set<Integer> markedForReview = new HashSet<>();

    private final AtomicBoolean submitted = new AtomicBoolean(false);
    private volatile ExamStatus submissionStatus = null;
    private volatile Instant endTime = null;
    private volatile ExamResult cachedResult = null;

    /**
     * Constructs an ExamSession with default real-time clock.
     *
     * @param questions       list of randomized questions for this attempt
     * @param durationSeconds duration of the exam in seconds
     */
    public ExamSession(List<Question> questions, int durationSeconds) {
        this(questions, durationSeconds, Instant::now);
    }

    /**
     * Constructs an ExamSession with an injectable clock supplier (for unit testing).
     *
     * @param questions       list of questions
     * @param durationSeconds duration in seconds
     * @param clock           time supplier
     */
    public ExamSession(List<Question> questions, int durationSeconds, Supplier<Instant> clock) {
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("Questions list must not be null or empty");
        }
        this.questions = Collections.unmodifiableList(new ArrayList<>(questions));
        this.totalDuration = Duration.ofSeconds(durationSeconds > 0 ? durationSeconds : AppConfig.DEFAULT_EXAM_DURATION_SECONDS);
        this.clock = (clock != null) ? clock : Instant::now;
        this.startTime = this.clock.get();
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public int getQuestionCount() {
        return questions.size();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public Question getCurrentQuestion() {
        return questions.get(currentIndex);
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public Duration getTotalDuration() {
        return totalDuration;
    }

    public boolean isSubmitted() {
        return submitted.get();
    }

    public ExamStatus getSubmissionStatus() {
        return submissionStatus;
    }

    // --- Navigation ---

    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    public boolean hasNext() {
        return currentIndex < questions.size() - 1;
    }

    public boolean previousQuestion() {
        if (hasPrevious()) {
            currentIndex--;
            return true;
        }
        return false;
    }

    public boolean nextQuestion() {
        if (hasNext()) {
            currentIndex++;
            return true;
        }
        return false;
    }

    public boolean jumpToQuestion(int index) {
        if (index >= 0 && index < questions.size()) {
            currentIndex = index;
            return true;
        }
        return false;
    }

    // --- Answer Management ---

    public void selectAnswer(int index, String optionLetter) {
        if (isSubmitted()) return;
        if (index >= 0 && index < questions.size()) {
            if (optionLetter != null && !optionLetter.trim().isEmpty()) {
                userAnswers.put(index, optionLetter.trim().toUpperCase());
            } else {
                userAnswers.remove(index);
            }
        }
    }

    public void selectCurrentAnswer(String optionLetter) {
        selectAnswer(currentIndex, optionLetter);
    }

    public void clearCurrentAnswer() {
        if (isSubmitted()) return;
        userAnswers.remove(currentIndex);
    }

    public String getAnswer(int index) {
        return userAnswers.get(index);
    }

    public String getCurrentAnswer() {
        return getAnswer(currentIndex);
    }

    public boolean isAnswered(int index) {
        return userAnswers.containsKey(index);
    }

    public int getAnsweredCount() {
        return userAnswers.size();
    }

    public int getUnansweredCount() {
        return questions.size() - getAnsweredCount();
    }

    // --- Review Marking ---

    public void toggleMarkForReview(int index) {
        if (isSubmitted()) return;
        if (markedForReview.contains(index)) {
            markedForReview.remove(index);
        } else {
            markedForReview.add(index);
        }
    }

    public void toggleCurrentMarkForReview() {
        toggleMarkForReview(currentIndex);
    }

    public boolean isMarkedForReview(int index) {
        return markedForReview.contains(index);
    }

    public boolean isCurrentMarkedForReview() {
        return isMarkedForReview(currentIndex);
    }

    // --- Time Tracking ---

    /**
     * Calculates the remaining time duration based on the configured clock.
     *
     * @return remaining duration (clamped to zero)
     */
    public Duration getRemainingDuration() {
        Instant now = clock.get();
        Duration elapsed = Duration.between(startTime, now);
        Duration remaining = totalDuration.minus(elapsed);
        if (remaining.isNegative()) {
            return Duration.ZERO;
        }
        return remaining;
    }

    public long getRemainingSeconds() {
        return getRemainingDuration().getSeconds();
    }

    public boolean isTimeExpired() {
        return getRemainingDuration().isZero();
    }

    // --- Submission & Scoring ---

    /**
     * Submits the exam attempt with the specified status.
     * This method is strictly thread-safe and idempotent: multiple invocations return the same result.
     *
     * @param status submission status (SUBMITTED, AUTO_SUBMITTED, or ABANDONED)
     * @return evaluated ExamResult
     */
    public synchronized ExamResult submit(ExamStatus status) {
        if (submitted.compareAndSet(false, true)) {
            this.submissionStatus = (status != null) ? status : ExamStatus.SUBMITTED;
            this.endTime = clock.get();
            this.cachedResult = computeResult();
        }
        return cachedResult;
    }

    private ExamResult computeResult() {
        int correctCount = 0;
        int incorrectCount = 0;
        int unansweredCount = 0;

        List<ExamResult.QuestionReviewItem> reviewItems = new ArrayList<>(questions.size());

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            String userOption = userAnswers.get(i); // e.g. "A", "B", "C", "D" or null
            String correctOption = q.getCorrectOption(); // e.g. "B"

            String userAnswerText;
            String correctAnswerText = q.getCorrectOptionText();
            String reviewStatus;

            if (userOption == null) {
                unansweredCount++;
                userAnswerText = "Not Answered";
                reviewStatus = "Not Answered";
            } else if (userOption.equalsIgnoreCase(correctOption)) {
                correctCount++;
                userAnswerText = userOption + ": " + q.getOptionText(userOption);
                reviewStatus = "Correct";
            } else {
                incorrectCount++;
                userAnswerText = userOption + ": " + q.getOptionText(userOption);
                reviewStatus = "Incorrect";
            }

            String formattedCorrect = correctOption + ": " + correctAnswerText;

            reviewItems.add(new ExamResult.QuestionReviewItem(
                    i,
                    q.getQuestionText(),
                    q.getCategory(),
                    userOption,
                    userAnswerText,
                    correctOption,
                    formattedCorrect,
                    reviewStatus,
                    markedForReview.contains(i)
            ));
        }

        int score = correctCount;
        int total = questions.size();
        double percentage = total > 0 ? (score * 100.0) / total : 0.0;
        boolean passed = percentage >= AppConfig.PASS_PERCENTAGE;

        long timeTakenSeconds = Duration.between(startTime, endTime).getSeconds();
        if (timeTakenSeconds < 0) timeTakenSeconds = 0;
        String formattedTimeTaken = formatDuration(timeTakenSeconds);

        return new ExamResult(
                score,
                total,
                percentage,
                passed,
                timeTakenSeconds,
                formattedTimeTaken,
                correctCount,
                incorrectCount,
                unansweredCount,
                submissionStatus,
                reviewItems
        );
    }

    public static String formatDuration(long seconds) {
        long mins = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }
}
