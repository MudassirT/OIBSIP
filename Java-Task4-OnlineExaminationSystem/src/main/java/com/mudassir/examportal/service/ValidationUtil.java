package com.mudassir.examportal.service;

import com.mudassir.examportal.config.AppConfig;

import java.util.regex.Pattern;

/**
 * Validation utilities for user input fields.
 */
public final class ValidationUtil {

    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile(AppConfig.DISPLAY_NAME_REGEX);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(AppConfig.PASSWORD_REGEX);

    private ValidationUtil() {
        // Prevent instantiation
    }

    /**
     * Validates a student display name.
     * Must be 2-40 characters, containing only letters, spaces, dots, and hyphens.
     *
     * @param displayName input name
     * @return true if valid, false otherwise
     */
    public static boolean isValidDisplayName(String displayName) {
        if (displayName == null) return false;
        String trimmed = displayName.trim();
        return DISPLAY_NAME_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validates a new password.
     * Must be at least 8 characters and contain at least one letter and one number.
     *
     * @param password input password string or char array
     * @return true if valid, false otherwise
     */
    public static boolean isValidNewPassword(char[] password) {
        if (password == null) return false;
        return isValidNewPassword(new String(password));
    }

    public static boolean isValidNewPassword(String password) {
        if (password == null) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validates non-empty input.
     *
     * @param text input text
     * @return true if non-null and not blank
     */
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isNotEmpty(char[] password) {
        return password != null && password.length > 0;
    }
}
