package com.mudassir.unitswitch.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Encapsulates the outcome of input validation.
 */
public class ValidationResult {

    private final boolean valid;
    private final double value;
    private final String errorMessage;
    private final boolean empty;
    private final boolean belowAbsoluteZero;

    private ValidationResult(boolean valid, double value, @Nullable String errorMessage, boolean empty, boolean belowAbsoluteZero) {
        this.valid = valid;
        this.value = value;
        this.errorMessage = errorMessage;
        this.empty = empty;
        this.belowAbsoluteZero = belowAbsoluteZero;
    }

    @NonNull
    public static ValidationResult success(double value) {
        return new ValidationResult(true, value, null, false, false);
    }

    @NonNull
    public static ValidationResult empty(@NonNull String errorMessage) {
        return new ValidationResult(false, 0.0, errorMessage, true, false);
    }

    @NonNull
    public static ValidationResult invalid(@NonNull String errorMessage) {
        return new ValidationResult(false, 0.0, errorMessage, false, false);
    }

    @NonNull
    public static ValidationResult absoluteZero(@NonNull String errorMessage) {
        return new ValidationResult(false, 0.0, errorMessage, false, true);
    }

    public boolean isValid() {
        return valid;
    }

    public double getValue() {
        return value;
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean isBelowAbsoluteZero() {
        return belowAbsoluteZero;
    }
}
