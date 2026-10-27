package com.mudassir.examportal.model;

import java.util.Objects;

/**
 * Model representing a recorded student exam attempt in the database.
 */
public class ExamAttempt {
    private int id;
    private int userId;
    private String startedAt; // ISO-8601 string or formatted timestamp
    private String endedAt;
    private int score;
    private int totalQuestions;
    private ExamStatus status;

    public ExamAttempt() {
    }

    public ExamAttempt(int id, int userId, String startedAt, String endedAt,
                       int score, int totalQuestions, ExamStatus status) {
        this.id = id;
        this.userId = userId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public String getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(String endedAt) {
        this.endedAt = endedAt;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public ExamStatus getStatus() {
        return status;
    }

    public void setStatus(ExamStatus status) {
        this.status = status;
    }

    public double getPercentage() {
        if (totalQuestions <= 0) return 0.0;
        return (score * 100.0) / totalQuestions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExamAttempt that = (ExamAttempt) o;
        return id == that.id && userId == that.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId);
    }

    @Override
    public String toString() {
        return "ExamAttempt{" +
                "id=" + id +
                ", userId=" + userId +
                ", score=" + score + "/" + totalQuestions +
                ", status=" + status +
                '}';
    }
}
