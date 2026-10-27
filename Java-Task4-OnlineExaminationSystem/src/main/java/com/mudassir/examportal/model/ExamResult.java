package com.mudassir.examportal.model;

import java.util.Collections;
import java.util.List;

/**
 * Model encapsulating the evaluated score and full question-by-question breakdown of an exam attempt.
 */
public class ExamResult {

    /**
     * Detail item for each question in the exam result breakdown.
     */
    public static class QuestionReviewItem {
        private final int questionIndex; // 0-based
        private final String questionText;
        private final String category;
        private final String userAnswerLetter; // e.g. "A" or null if unanswered
        private final String userAnswerText;   // text or "Not Answered"
        private final String correctAnswerLetter; // e.g. "B"
        private final String correctAnswerText;
        private final String status; // "Correct", "Incorrect", "Not Answered"
        private final boolean markedForReview;

        public QuestionReviewItem(int questionIndex, String questionText, String category,
                                  String userAnswerLetter, String userAnswerText,
                                  String correctAnswerLetter, String correctAnswerText,
                                  String status, boolean markedForReview) {
            this.questionIndex = questionIndex;
            this.questionText = questionText;
            this.category = category;
            this.userAnswerLetter = userAnswerLetter;
            this.userAnswerText = userAnswerText;
            this.correctAnswerLetter = correctAnswerLetter;
            this.correctAnswerText = correctAnswerText;
            this.status = status;
            this.markedForReview = markedForReview;
        }

        public int getQuestionIndex() {
            return questionIndex;
        }

        public int getQuestionNumber() {
            return questionIndex + 1;
        }

        public String getQuestionText() {
            return questionText;
        }

        public String getCategory() {
            return category;
        }

        public String getUserAnswerLetter() {
            return userAnswerLetter;
        }

        public String getUserAnswerText() {
            return userAnswerText;
        }

        public String getCorrectAnswerLetter() {
            return correctAnswerLetter;
        }

        public String getCorrectAnswerText() {
            return correctAnswerText;
        }

        public String getStatus() {
            return status;
        }

        public boolean isMarkedForReview() {
            return markedForReview;
        }

        public boolean isCorrect() {
            return "Correct".equalsIgnoreCase(status);
        }

        public boolean isUnanswered() {
            return "Not Answered".equalsIgnoreCase(status);
        }
    }

    private final int score;
    private final int totalQuestions;
    private final double percentage;
    private final boolean passed;
    private final long timeTakenSeconds;
    private final String timeTakenFormatted;
    private final int correctCount;
    private final int incorrectCount;
    private final int unansweredCount;
    private final ExamStatus status;
    private final List<QuestionReviewItem> reviewItems;

    public ExamResult(int score, int totalQuestions, double percentage, boolean passed,
                      long timeTakenSeconds, String timeTakenFormatted,
                      int correctCount, int incorrectCount, int unansweredCount,
                      ExamStatus status, List<QuestionReviewItem> reviewItems) {
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.percentage = percentage;
        this.passed = passed;
        this.timeTakenSeconds = timeTakenSeconds;
        this.timeTakenFormatted = timeTakenFormatted;
        this.correctCount = correctCount;
        this.incorrectCount = incorrectCount;
        this.unansweredCount = unansweredCount;
        this.status = status;
        this.reviewItems = (reviewItems != null) ? Collections.unmodifiableList(reviewItems) : Collections.emptyList();
    }

    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public double getPercentage() {
        return percentage;
    }

    public boolean isPassed() {
        return passed;
    }

    public long getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public String getTimeTakenFormatted() {
        return timeTakenFormatted;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getIncorrectCount() {
        return incorrectCount;
    }

    public int getUnansweredCount() {
        return unansweredCount;
    }

    public ExamStatus getStatus() {
        return status;
    }

    public boolean isAutoSubmitted() {
        return status == ExamStatus.AUTO_SUBMITTED;
    }

    public List<QuestionReviewItem> getReviewItems() {
        return reviewItems;
    }

    public String getScoreSummary() {
        return score + " out of " + totalQuestions;
    }

    @Override
    public String toString() {
        return "ExamResult{" +
                "score=" + score +
                ", totalQuestions=" + totalQuestions +
                ", percentage=" + String.format("%.1f%%", percentage) +
                ", passed=" + passed +
                ", status=" + status +
                '}';
    }
}
