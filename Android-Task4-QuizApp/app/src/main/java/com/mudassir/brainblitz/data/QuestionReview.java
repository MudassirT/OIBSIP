package com.mudassir.brainblitz.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model class storing review details for a question answered during a quiz round.
 */
public class QuestionReview implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String questionText;
    private final String category;
    private final List<String> options;
    private final int selectedOptionIndex;
    private final int correctOptionIndex;
    private final boolean isCorrect;
    private final boolean isTimedOut;
    private final String explanation;

    public QuestionReview(String questionText, String category, List<String> options,
                          int selectedOptionIndex, int correctOptionIndex,
                          boolean isCorrect, boolean isTimedOut, String explanation) {
        this.questionText = questionText;
        this.category = category;
        this.options = new ArrayList<>(options);
        this.selectedOptionIndex = selectedOptionIndex;
        this.correctOptionIndex = correctOptionIndex;
        this.isCorrect = isCorrect;
        this.isTimedOut = isTimedOut;
        this.explanation = explanation;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public int getSelectedOptionIndex() {
        return selectedOptionIndex;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public boolean isTimedOut() {
        return isTimedOut;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getCorrectOptionText() {
        if (correctOptionIndex >= 0 && correctOptionIndex < options.size()) {
            return options.get(correctOptionIndex);
        }
        return "";
    }

    public String getSelectedOptionText() {
        if (selectedOptionIndex >= 0 && selectedOptionIndex < options.size()) {
            return options.get(selectedOptionIndex);
        }
        return "Timed Out / Skipped";
    }
}
