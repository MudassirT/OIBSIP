package com.mudassir.unitswitch.util;

/**
 * Global constants for UnitSwitch application.
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    // SharedPreferences
    public static final String PREFS_NAME = "unitswitch_prefs";
    public static final String KEY_LAST_CATEGORY = "key_last_category";
    public static final String KEY_LIVE_CONVERSION = "key_live_conversion";

    // SavedInstanceState Bundle Keys
    public static final String KEY_STATE_CATEGORY = "state_category";
    public static final String KEY_STATE_SOURCE_UNIT = "state_source_unit";
    public static final String KEY_STATE_TARGET_UNIT = "state_target_unit";
    public static final String KEY_STATE_INPUT_TEXT = "state_input_text";
    public static final String KEY_STATE_RESULT_SHOWN = "state_result_shown";
    public static final String KEY_STATE_RESULT_LARGE = "state_result_large";
    public static final String KEY_STATE_RESULT_EQUATION = "state_result_equation";
    public static final String KEY_STATE_RESULT_FORMULA = "state_result_formula";

    // Temperature Absolute Zero limits
    public static final double ABSOLUTE_ZERO_CELSIUS = -273.15;
    public static final double ABSOLUTE_ZERO_FAHRENHEIT = -459.67;
    public static final double ABSOLUTE_ZERO_KELVIN = 0.0;

    // Numerical thresholds for formatting
    public static final double MAX_EXPONENTIAL_THRESHOLD = 1e12;
    public static final double MIN_EXPONENTIAL_THRESHOLD = 1e-6;
    public static final int MAX_DECIMAL_PLACES = 6;
}
