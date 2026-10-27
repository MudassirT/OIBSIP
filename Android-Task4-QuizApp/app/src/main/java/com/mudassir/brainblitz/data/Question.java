package com.mudassir.brainblitz.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model class representing a quiz question.
 */
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String category;
    private final String questionText;
    private final List<String> options;
    private final int correctOptionIndex;
    private final String explanation;

    public Question(int id, String category, String questionText, List<String> options, int correctOptionIndex, String explanation) {
        this.id = id;
        this.category = category != null ? category : "General Tech";
        this.questionText = questionText;
        this.options = new ArrayList<>(options);
        this.correctOptionIndex = correctOptionIndex;
        this.explanation = explanation != null ? explanation : "";
    }

    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
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

    /**
     * Creates a new Question instance with shuffled options,
     * remapping the correctOptionIndex to match the new position of the correct answer.
     */
    public Question createShuffledCopy() {
        String correctText = getCorrectOptionText();
        List<String> shuffledOptions = new ArrayList<>(this.options);
        Collections.shuffle(shuffledOptions);

        int newCorrectIndex = shuffledOptions.indexOf(correctText);
        if (newCorrectIndex == -1) {
            // Fallback safety check
            newCorrectIndex = 0;
        }

        return new Question(this.id, this.category, this.questionText, shuffledOptions, newCorrectIndex, this.explanation);
    }
}
