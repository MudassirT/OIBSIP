package com.mudassir.unitswitch.util;

import androidx.annotation.NonNull;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility class for robust, locale-independent number and equation formatting.
 * Uses US English locale to prevent decimal point parsing issues on European/comma devices.
 */
public final class FormatUtils {

    private FormatUtils() {
        // Prevent instantiation
    }

    /**
     * Formats a floating-point value cleanly:
     * - Strips trailing zeros after decimal point
     * - Up to 6 significant decimal places with HALF_UP rounding
     * - Uses scientific notation only for extremely large or small numbers
     * - Normalizes negative zero (-0.0) to 0
     * - Guards against NaN and Infinite values
     *
     * @param value The double value to format
     * @return Formatted string representation
     */
    @NonNull
    public static String formatNumber(double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? "Infinity" : "-Infinity";
        }

        // Normalize negative zero or negligible noise
        if (Math.abs(value) < 1e-12) {
            return "0";
        }

        double absValue = Math.abs(value);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

        // Scientific notation for numbers outside sensible human-readable scales
        if (absValue >= Constants.MAX_EXPONENTIAL_THRESHOLD || absValue < Constants.MIN_EXPONENTIAL_THRESHOLD) {
            DecimalFormat sciDf = new DecimalFormat("0.######E0", symbols);
            sciDf.setRoundingMode(RoundingMode.HALF_UP);
            return sciDf.format(value);
        }

        // Standard decimal format: up to 6 decimal places, no grouping commas, no trailing zero noise
        DecimalFormat standardDf = new DecimalFormat("0.######", symbols);
        standardDf.setGroupingUsed(false);
        standardDf.setRoundingMode(RoundingMode.HALF_UP);

        return standardDf.format(value);
    }

    /**
     * Formats the complete equation string.
     * Example: "100 cm = 39.3701 in"
     *
     * @param inputValue  Raw input value
     * @param inputSymbol Source unit symbol (e.g. "cm")
     * @param resultValue Converted output value
     * @param targetSymbol Target unit symbol (e.g. "in")
     * @return Formatted equation string
     */
    @NonNull
    public static String formatEquation(double inputValue,
                                        @NonNull String inputSymbol,
                                        double resultValue,
                                        @NonNull String targetSymbol) {
        return formatNumber(inputValue) + " " + inputSymbol + " = " + formatNumber(resultValue) + " " + targetSymbol;
    }
}
