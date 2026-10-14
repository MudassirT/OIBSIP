package com.mudassir.taskvault.data;

/**
 * Model class representing an individual task owned by a user.
 */
public class Task {
    private final long id;
    private final long userId;
    private final String title;
    private final String notes;
    private final boolean isCompleted;
    private final long createdAt;
    private final long completedAt;

    public Task(long id, long userId, String title, String notes, boolean isCompleted, long createdAt, long completedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.notes = notes;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getCompletedAt() {
        return completedAt;
    }
}
