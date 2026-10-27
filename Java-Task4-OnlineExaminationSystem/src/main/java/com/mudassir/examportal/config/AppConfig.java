package com.mudassir.examportal.config;

/**
 * Central configuration constants for the ExamPortal application.
 * Eliminates magic strings and numbers throughout the codebase.
 */
public final class AppConfig {

    private AppConfig() {
        // Prevent instantiation
    }

    // Application Metadata
    public static final String APP_NAME = "ExamPortal";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_TITLE = APP_NAME + " - Online Examination System";
    public static final String AUTHOR_NAME = "Mudassir Ahmed";
    public static final String INTERNSHIP_TITLE = "Oasis Infobyte Java Development Internship - Task 4";

    // System Properties for Runtime Overrides
    public static final String PROP_EXAM_DURATION = "exam.duration.seconds";
    public static final String PROP_DB_PATH = "exam.db.path";

    // Default Exam Rules
    public static final int DEFAULT_EXAM_DURATION_SECONDS = 30 * 60; // 30 minutes (1800s)
    public static final int QUESTIONS_PER_EXAM = 10;
    public static final double PASS_PERCENTAGE = 40.0; // 40% passing threshold
    public static final int OPTIONS_PER_QUESTION = 4;

    // Timer & Rate Limiting Thresholds
    public static final int TIMER_AMBER_THRESHOLD_SECONDS = 5 * 60; // 5 minutes
    public static final int TIMER_RED_THRESHOLD_SECONDS = 60;       // 1 minute
    public static final int TIMER_TICK_INTERVAL_MS = 500;           // Timer poll frequency
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    public static final int LOCKOUT_DURATION_SECONDS = 30;

    // Database Configuration
    public static final String DEFAULT_DB_PATH = "./data/examportal.db";
    public static final String DB_JDBC_PREFIX = "jdbc:sqlite:";

    // Screen Names for CardLayout
    public static final String CARD_LOGIN = "CARD_LOGIN";
    public static final String CARD_PROFILE = "CARD_PROFILE";
    public static final String CARD_EXAM = "CARD_EXAM";
    public static final String CARD_RESULT = "CARD_RESULT";

    // Demo Accounts
    public static final String DEMO_STUDENT1_USER = "student1";
    public static final String DEMO_STUDENT1_PASS = "Student@123";
    public static final String DEMO_STUDENT1_NAME = "Mudassir Student One";

    public static final String DEMO_STUDENT2_USER = "student2";
    public static final String DEMO_STUDENT2_PASS = "Student@456";
    public static final String DEMO_STUDENT2_NAME = "Amina Student Two";

    // Validation Rules
    public static final int MIN_DISPLAY_NAME_LENGTH = 2;
    public static final int MAX_DISPLAY_NAME_LENGTH = 40;
    public static final String DISPLAY_NAME_REGEX = "^[a-zA-Z\\s.-]{2,40}$";
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";

    /**
     * Resolves the configured exam duration in seconds.
     * Allows dynamic override via `-Dexam.duration.seconds=30`.
     *
     * @return exam duration in seconds
     */
    public static int getExamDurationSeconds() {
        String prop = System.getProperty(PROP_EXAM_DURATION);
        if (prop != null && !prop.trim().isEmpty()) {
            try {
                int seconds = Integer.parseInt(prop.trim());
                if (seconds > 0) {
                    return seconds;
                }
            } catch (NumberFormatException ignored) {
                // Fall back to default
            }
        }
        return DEFAULT_EXAM_DURATION_SECONDS;
    }

    /**
     * Resolves the database JDBC URL.
     * Allows dynamic override via `-Dexam.db.path=...` (e.g. `:memory:` for testing).
     *
     * @return JDBC connection string
     */
    public static String getDatabaseUrl() {
        String prop = System.getProperty(PROP_DB_PATH);
        if (prop != null && !prop.trim().isEmpty()) {
            return DB_JDBC_PREFIX + prop.trim();
        }
        return DB_JDBC_PREFIX + DEFAULT_DB_PATH;
    }
}
