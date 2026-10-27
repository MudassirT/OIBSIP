package com.mudassir.unitswitch.logic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Encapsulates the result of a unit conversion attempt.
 */
public class ConversionResult {

    private final boolean success;
    private final double resultValue;
    private final String errorMessage;
    private final boolean belowAbsoluteZero;

    private ConversionResult(boolean success, double resultValue, @Nullable String errorMessage, boolean belowAbsoluteZero) {
        this.success = success;
        this.resultValue = resultValue;
        this.errorMessage = errorMessage;
        this.belowAbsoluteZero = belowAbsoluteZero;
    }

    @NonNull
    public static ConversionResult success(double resultValue) {
        return new ConversionResult(true, resultValue, null, false);
    }

    @NonNull
    public static ConversionResult error(@NonNull String errorMessage) {
        return new ConversionResult(false, 0.0, errorMessage, false);
    }

    @NonNull
    public static ConversionResult absoluteZeroError(@NonNull String errorMessage) {
        return new ConversionResult(false, 0.0, errorMessage, true);
    }

    public boolean isSuccess() {
        return success;
    }

    public double getResultValue() {
        return resultValue;
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isBelowAbsoluteZero() {
        return belowAbsoluteZero;
    }
}
