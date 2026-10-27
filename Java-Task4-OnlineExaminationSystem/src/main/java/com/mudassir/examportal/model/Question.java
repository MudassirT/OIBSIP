package com.mudassir.examportal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Model representing a multiple-choice question.
 */
public class Question {
    private int id;
    private String category;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption; // 'A', 'B', 'C', or 'D'

    public Question() {
    }

    public Question(int id, String category, String questionText,
                    String optionA, String optionB, String optionC, String optionD,
                    String correctOption) {
        this.id = id;
        this.category = category;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = (correctOption != null) ? correctOption.toUpperCase() : "A";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = (correctOption != null) ? correctOption.toUpperCase() : "A";
    }

    /**
     * Retrieves the text corresponding to the option letter (A, B, C, D).
     *
     * @param letter option letter
     * @return option text or empty string if not found
     */
    public String getOptionText(String letter) {
        if (letter == null) return "";
        switch (letter.toUpperCase()) {
            case "A": return optionA != null ? optionA : "";
            case "B": return optionB != null ? optionB : "";
            case "C": return optionC != null ? optionC : "";
            case "D": return optionD != null ? optionD : "";
            default: return "";
        }
    }

    /**
     * Retrieves the text of the correct option.
     *
     * @return text of the correct answer
     */
    public String getCorrectOptionText() {
        return getOptionText(correctOption);
    }

    /**
     * Returns an unmodifiable list of the 4 options.
     *
     * @return list of options in order A, B, C, D
     */
    public List<String> getOptionsList() {
        List<String> list = new ArrayList<>(4);
        list.add(optionA);
        list.add(optionB);
        list.add(optionC);
        list.add(optionD);
        return Collections.unmodifiableList(list);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return id == question.id && Objects.equals(questionText, question.questionText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, questionText);
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", questionText='" + questionText + '\'' +
                ", correctOption='" + correctOption + '\'' +
                '}';
    }
}
