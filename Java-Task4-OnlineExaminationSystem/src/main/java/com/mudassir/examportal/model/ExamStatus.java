package com.mudassir.examportal.model;

/**
 * Enumeration representing the completion status of an exam attempt.
 */
public enum ExamStatus {
    SUBMITTED("Submitted"),
    AUTO_SUBMITTED("Auto-Submitted"),
    ABANDONED("Abandoned");

    private final String displayName;

    ExamStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ExamStatus fromString(String text) {
        if (text == null) return SUBMITTED;
        for (ExamStatus status : values()) {
            if (status.name().equalsIgnoreCase(text) || status.displayName.equalsIgnoreCase(text)) {
                return status;
            }
        }
        return SUBMITTED;
    }
}
