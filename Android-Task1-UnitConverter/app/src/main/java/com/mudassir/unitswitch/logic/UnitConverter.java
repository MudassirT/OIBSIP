package com.mudassir.unitswitch.logic;

import androidx.annotation.NonNull;

import com.mudassir.unitswitch.util.Constants;

/**
 * Core conversion logic for UnitSwitch.
 * Uses a base-unit architecture for linear categories (Length, Weight, Volume, Speed)
 * and dedicated thermodynamic formulas with absolute-zero validation for Temperature.
 */
public class UnitConverter {

    private static final double EPSILON = 1e-9;

    /**
     * Converts a numeric value from a source unit to a target unit within a category.
     *
     * @param category   Measurement category (Length, Weight, Temperature, etc.)
     * @param fromUnit   The unit to convert from
     * @param toUnit     The unit to convert to
     * @param inputValue The numeric input value
     * @return ConversionResult containing success status, value, and any error message
     */
    @NonNull
    public ConversionResult convert(@NonNull Category category,
                                   @NonNull UnitModel fromUnit,
                                   @NonNull UnitModel toUnit,
                                   double inputValue) {

        // Guard against non-finite input values
        if (Double.isNaN(inputValue) || Double.isInfinite(inputValue)) {
            return ConversionResult.error("Value must be a finite number");
        }

        // Temperature handling (non-linear)
        if (category.isTemperature()) {
            return convertTemperature(fromUnit, toUnit, inputValue);
        }

        // Linear categories: Base-unit architecture
        // 1. Same-unit conversion returns the exact same value
        if (fromUnit.equals(toUnit)) {
            return ConversionResult.success(inputValue);
        }

        // 2. Convert from source unit to base unit
        double baseValue = inputValue * fromUnit.getFactorToBase();

        // 3. Convert from base unit to target unit
        double targetFactor = toUnit.getFactorToBase();
        if (targetFactor == 0.0) {
            return ConversionResult.error("Invalid target unit configuration");
        }
        double targetValue = baseValue / targetFactor;

        // Guard against numerical overflow / NaN
        if (Double.isNaN(targetValue) || Double.isInfinite(targetValue)) {
            return ConversionResult.error("Result exceeds computable limits");
        }

        return ConversionResult.success(targetValue);
    }

    /**
     * Converts temperature values across Celsius, Fahrenheit, and Kelvin.
     * Validates against physical absolute zero limits.
     */
    @NonNull
    private ConversionResult convertTemperature(@NonNull UnitModel fromUnit,
                                                @NonNull UnitModel toUnit,
                                                double inputValue) {
        String fromId = fromUnit.getId();
        String toId = toUnit.getId();

        // 1. Absolute zero validation on input
        if (isBelowAbsoluteZero(fromId, inputValue)) {
            return ConversionResult.absoluteZeroError(
                    "Value is below absolute zero (-273.15 °C / -459.67 °F / 0 K)"
            );
        }

        // 2. Identity conversion
        if (fromId.equalsIgnoreCase(toId)) {
            return ConversionResult.success(inputValue);
        }

        // 3. Convert from source unit to Celsius (intermediate base)
        double celsius;
        switch (fromId.toUpperCase()) {
            case "C":
                celsius = inputValue;
                break;
            case "F":
                celsius = (inputValue - 32.0) * 5.0 / 9.0;
                break;
            case "K":
                celsius = inputValue - 273.15;
                break;
            default:
                return ConversionResult.error("Unsupported source temperature unit: " + fromId);
        }

        // Double-check celsius is not below absolute zero (due to precision edge cases)
        if (celsius < Constants.ABSOLUTE_ZERO_CELSIUS - EPSILON) {
            return ConversionResult.absoluteZeroError(
                    "Value is below absolute zero (-273.15 °C / -459.67 °F / 0 K)"
            );
        }

        // 4. Convert from Celsius to target unit
        double targetValue;
        switch (toId.toUpperCase()) {
            case "C":
                targetValue = celsius;
                break;
            case "F":
                targetValue = (celsius * 9.0 / 5.0) + 32.0;
                break;
            case "K":
                targetValue = celsius + 273.15;
                break;
            default:
                return ConversionResult.error("Unsupported target temperature unit: " + toId);
        }

        if (Double.isNaN(targetValue) || Double.isInfinite(targetValue)) {
            return ConversionResult.error("Result exceeds computable limits");
        }

        return ConversionResult.success(targetValue);
    }

    /**
     * Validates whether a temperature value is strictly below physical absolute zero.
     */
    public boolean isBelowAbsoluteZero(@NonNull String unitId, double value) {
        switch (unitId.toUpperCase()) {
            case "C":
                return value < (Constants.ABSOLUTE_ZERO_CELSIUS - EPSILON);
            case "F":
                return value < (Constants.ABSOLUTE_ZERO_FAHRENHEIT - EPSILON);
            case "K":
                return value < (Constants.ABSOLUTE_ZERO_KELVIN - EPSILON);
            default:
                return false;
        }
    }
}
