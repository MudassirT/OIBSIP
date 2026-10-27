package com.mudassir.brainblitz.util;

/**
 * Global constants for the BrainBlitz application.
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    public static final int QUESTIONS_PER_ROUND = 10;
    public static final int QUESTION_TIMER_SECONDS = 25;
    public static final long TIMER_INTERVAL_MILLIS = 1000L;

    public static final String PREFS_NAME = "brainblitz_prefs";
    public static final String KEY_BEST_SCORE = "key_best_score";
    public static final String KEY_BEST_PERCENTAGE = "key_best_percentage";
    public static final String KEY_TOTAL_GAMES = "key_total_games";

    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_TOTAL_QUESTIONS = "extra_total_questions";
    public static final String EXTRA_CORRECT_COUNT = "extra_correct_count";
    public static final String EXTRA_INCORRECT_COUNT = "extra_incorrect_count";
    public static final String EXTRA_TIMED_OUT_COUNT = "extra_timed_out_count";
    public static final String EXTRA_IS_NEW_HIGH_SCORE = "extra_is_new_high_score";
    public static final String EXTRA_REVIEW_LIST = "extra_review_list";
}
