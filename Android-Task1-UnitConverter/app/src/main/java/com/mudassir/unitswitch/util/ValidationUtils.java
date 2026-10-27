package com.mudassir.unitswitch.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mudassir.unitswitch.logic.Category;
import com.mudassir.unitswitch.logic.UnitConverter;
import com.mudassir.unitswitch.logic.UnitModel;

/**
 * Validates raw user input strings for numeric validity, format constraints,
 * locale considerations (comma to period conversion), and physical limits like absolute zero.
 */
public final class ValidationUtils {

    private static final UnitConverter CONVERTER = new UnitConverter();

    private ValidationUtils() {
        // Prevent instantiation
    }

    /**
     * Performs comprehensive validation on the user input text.
     *
     * @param rawInput   The raw text from the input field
     * @param category   Current measurement category
     * @param sourceUnit Current source unit
     * @return ValidationResult indicating success or specific validation error
     */
    @NonNull
    public static ValidationResult validateInput(@Nullable String rawInput,
                                                @NonNull Category category,
                                                @NonNull UnitModel sourceUnit) {

        if (rawInput == null) {
            return ValidationResult.empty("Please enter a value to convert");
        }

        String trimmed = rawInput.trim();
        if (trimmed.isEmpty()) {
            return ValidationResult.empty("Please enter a value to convert");
        }

        // Catch lone signs or lone decimal points before parsing
        if (trimmed.equals("-") || trimmed.equals("+") || trimmed.equals(".") ||
                trimmed.equals(",") || trimmed.equals("-.") || trimmed.equals("-,")) {
            return ValidationResult.invalid("Please enter a complete numeric value");
        }

        // Support European comma decimal input transparently
        String normalized = trimmed.replace(',', '.');

        // Check for multiple decimal points
        int firstDot = normalized.indexOf('.');
        if (firstDot != -1 && normalized.indexOf('.', firstDot + 1) != -1) {
            return ValidationResult.invalid("Invalid number: multiple decimal points found");
        }

        double parsedValue;
        try {
            parsedValue = Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return ValidationResult.invalid("Please enter a valid numeric value");
        }

        if (Double.isNaN(parsedValue) || Double.isInfinite(parsedValue)) {
            return ValidationResult.invalid("Value is outside allowed numeric limits");
        }

        // Check absolute zero boundary for temperature
        if (category.isTemperature()) {
            if (CONVERTER.isBelowAbsoluteZero(sourceUnit.getId(), parsedValue)) {
                return ValidationResult.absoluteZero(
                        "Value cannot be below absolute zero (-273.15 °C / -459.67 °F / 0 K)"
                );
            }
        }

        return ValidationResult.success(parsedValue);
    }
}
