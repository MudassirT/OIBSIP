package com.mudassir.taskvault.util;

import java.util.regex.Pattern;

/**
 * Validation helper containing validation rules for user input and tasks.
 */
public final class Validators {

    // RFC 5322 compatible simplified email pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}$"
    );

    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_TASK_TITLE_LENGTH = 100;
    public static final int MAX_TASK_NOTES_LENGTH = 500;

    private Validators() {
        // Prevent instantiation
    }

    /**
     * Checks if a name is non-null and not empty after trimming.
     */
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    /**
     * Validates whether an email string matches a standard valid email format.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Checks whether a password has at least 8 characters.
     */
    public static boolean isPasswordLengthValid(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    /**
     * Checks whether a password contains at least one numeric digit.
     */
    public static boolean doesPasswordContainDigit(String password) {
        if (password == null) {
            return false;
        }
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether password and confirm password match exactly.
     */
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Checks if a task title is non-null and not empty after trimming whitespace.
     */
    public static boolean isValidTaskTitle(String title) {
        return title != null && !title.trim().isEmpty() && title.trim().length() <= MAX_TASK_TITLE_LENGTH;
    }

    /**
     * Checks if task notes do not exceed maximum character length.
     */
    public static boolean areTaskNotesValid(String notes) {
        if (notes == null) {
            return true;
        }
        return notes.length() <= MAX_TASK_NOTES_LENGTH;
    }
}
