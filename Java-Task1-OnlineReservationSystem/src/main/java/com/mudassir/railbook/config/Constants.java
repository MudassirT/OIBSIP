package com.mudassir.railbook.config;

/**
 * Application-wide constants for RailBook Online Reservation System.
 * Eliminates magic strings and numbers throughout the codebase.
 *
 * @author Mudassir Ahmed
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation of utility constant class
    }

    // Application Metadata
    public static final String APP_NAME = "RailBook";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_SUBTITLE = "Online Train Reservation System";

    // Database Configuration
    public static final String DEFAULT_DB_DIR = "./data";
    public static final String DEFAULT_DB_NAME = "railbook.db";
    public static final String DEFAULT_DB_PATH = DEFAULT_DB_DIR + "/" + DEFAULT_DB_NAME;
    public static final String JDBC_SQLITE_PREFIX = "jdbc:sqlite:";

    // Database Tables
    public static final String TABLE_USERS = "users";
    public static final String TABLE_TRAINS = "trains";
    public static final String TABLE_STATIONS = "stations";
    public static final String TABLE_RESERVATIONS = "reservations";
    public static final String TABLE_CANCELLATION_LOG = "cancellation_log";

    // Column Names - Users
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_USERNAME = "username";
    public static final String COL_USER_PASSWORD_HASH = "password_hash";
    public static final String COL_USER_SALT = "salt";

    // Column Names - Trains
    public static final String COL_TRAIN_NUMBER = "train_number";
    public static final String COL_TRAIN_NAME = "train_name";
    public static final String COL_TRAIN_SOURCE = "source_station";
    public static final String COL_TRAIN_DESTINATION = "destination_station";

    // Column Names - Stations
    public static final String COL_STATION_NAME = "name";

    // Column Names - Reservations
    public static final String COL_RES_PNR = "pnr";
    public static final String COL_RES_USER_ID = "user_id";
    public static final String COL_RES_PASSENGER_NAME = "passenger_name";
    public static final String COL_RES_TRAIN_NUMBER = "train_number";
    public static final String COL_RES_CLASS_TYPE = "class_type";
    public static final String COL_RES_JOURNEY_DATE = "journey_date";
    public static final String COL_RES_SOURCE = "source";
    public static final String COL_RES_DESTINATION = "destination";
    public static final String COL_RES_BOOKED_AT = "booked_at";

    // Column Names - Cancellation Log
    public static final String COL_LOG_ID = "id";
    public static final String COL_LOG_PNR = "pnr";
    public static final String COL_LOG_CANCELLED_BY = "cancelled_by";
    public static final String COL_LOG_CANCELLED_AT = "cancelled_at";

    // Class Types
    public static final String CLASS_AC_FIRST = "AC First";
    public static final String CLASS_AC_BUSINESS = "AC Business";
    public static final String CLASS_ECONOMY = "Economy";
    public static final String CLASS_SLEEPER = "Sleeper";
    public static final String[] ALL_CLASS_TYPES = {
            CLASS_AC_FIRST,
            CLASS_AC_BUSINESS,
            CLASS_ECONOMY,
            CLASS_SLEEPER
    };

    // Date & Time Formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // Security & Lockout Settings
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    public static final long LOCKOUT_DURATION_SECONDS = 30L;
    public static final int HASH_ITERATIONS = 65536;
    public static final int HASH_KEY_LENGTH = 256;
    public static final int SALT_BYTE_LENGTH = 16;

    // Validation Limits
    public static final int MIN_PASSENGER_NAME_LENGTH = 2;
    public static final int MAX_PASSENGER_NAME_LENGTH = 60;
    public static final int PNR_LENGTH = 10;
    public static final int MAX_DAYS_IN_ADVANCE = 120;
    public static final int MAX_PNR_GENERATION_RETRIES = 5;

    // UI Dimensions
    public static final int MAIN_WINDOW_WIDTH = 950;
    public static final int MAIN_WINDOW_HEIGHT = 650;
    public static final int LOGIN_WINDOW_WIDTH = 480;
    public static final int LOGIN_WINDOW_HEIGHT = 520;
}
