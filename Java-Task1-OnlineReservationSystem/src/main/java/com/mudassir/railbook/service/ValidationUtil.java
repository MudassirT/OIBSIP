package com.mudassir.railbook.service;

import com.mudassir.railbook.config.Constants;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Utility class handling all domain and form input validation.
 * Trims inputs, enforces security constraints, and provides user-friendly error messages.
 *
 * @author Mudassir Ahmed
 */
public final class ValidationUtil {

    // Regex patterns
    private static final Pattern PASSENGER_NAME_PATTERN = Pattern.compile("^[A-Za-z .'-]+$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern PNR_PATTERN = Pattern.compile("^[0-9]{" + Constants.PNR_LENGTH + "}$");

    // Strict ISO date formatter that enforces valid calendar days (e.g. rejects 2026-02-30)
    private static final DateTimeFormatter STRICT_DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    private ValidationUtil() {
        // Utility class
    }

    /**
     * Validates a passenger's full name.
     * Must be 2-60 characters, containing only letters, spaces, dots, apostrophes, and hyphens.
     *
     * @param rawName passenger name
     * @return null if valid, or an error message string
     */
    public static String validatePassengerName(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return "Passenger name cannot be empty.";
        }
        String trimmed = rawName.trim();
        if (trimmed.length() < Constants.MIN_PASSENGER_NAME_LENGTH || trimmed.length() > Constants.MAX_PASSENGER_NAME_LENGTH) {
            return "Passenger name must be between " + Constants.MIN_PASSENGER_NAME_LENGTH +
                    " and " + Constants.MAX_PASSENGER_NAME_LENGTH + " characters.";
        }
        if (!PASSENGER_NAME_PATTERN.matcher(trimmed).matches()) {
            return "Passenger name may only contain letters, spaces, dots, apostrophes, and hyphens.";
        }
        return null;
    }

    /**
     * Validates a train number string.
     * Must be numeric and positive integer.
     *
     * @param rawTrainNumber train number string
     * @return null if valid, or an error message string
     */
    public static String validateTrainNumberString(String rawTrainNumber) {
        if (rawTrainNumber == null || rawTrainNumber.trim().isEmpty()) {
            return "Train number cannot be empty.";
        }
        String trimmed = rawTrainNumber.trim();
        if (!NUMERIC_PATTERN.matcher(trimmed).matches()) {
            return "Train number must be numeric.";
        }
        try {
            int num = Integer.parseInt(trimmed);
            if (num <= 0) {
                return "Train number must be a positive integer.";
            }
        } catch (NumberFormatException e) {
            return "Train number is out of allowable range.";
        }
        return null;
    }

    /**
     * Validates journey date string.
     * Must be in yyyy-MM-dd format, a real calendar date, not in the past,
     * and not more than 120 days in advance.
     *
     * @param rawDate date string
     * @return null if valid, or an error message string
     */
    public static String validateJourneyDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return "Journey date cannot be empty.";
        }
        String trimmed = rawDate.trim();
        if (trimmed.length() != 10) {
            return "Journey date must follow the format YYYY-MM-DD (e.g. 2026-10-20).";
        }

        LocalDate date;
        try {
            date = LocalDate.parse(trimmed, STRICT_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return "Invalid calendar date. Please enter a valid date in YYYY-MM-DD format (e.g. 2026-10-20).";
        }

        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            return "Journey date cannot be in the past.";
        }

        LocalDate maxDate = today.plusDays(Constants.MAX_DAYS_IN_ADVANCE);
        if (date.isAfter(maxDate)) {
            return "Journey date cannot be more than " + Constants.MAX_DAYS_IN_ADVANCE +
                    " days in advance (latest: " + maxDate + ").";
        }

        return null;
    }

    /**
     * Validates source and destination stations.
     * Neither can be empty, and source must be different from destination.
     *
     * @param rawSource      source station
     * @param rawDestination destination station
     * @return null if valid, or an error message string
     */
    public static String validateStations(String rawSource, String rawDestination) {
        if (rawSource == null || rawSource.trim().isEmpty()) {
            return "Source station cannot be empty.";
        }
        if (rawDestination == null || rawDestination.trim().isEmpty()) {
            return "Destination station cannot be empty.";
        }
        String source = rawSource.trim();
        String destination = rawDestination.trim();
        if (source.equalsIgnoreCase(destination)) {
            return "Source and destination stations cannot be the same.";
        }
        return null;
    }

    /**
     * Validates travel class type.
     *
     * @param rawClass class type
     * @return null if valid, or an error message string
     */
    public static String validateClassType(String rawClass) {
        if (rawClass == null || rawClass.trim().isEmpty()) {
            return "Class type cannot be empty.";
        }
        String trimmed = rawClass.trim();
        boolean match = Arrays.asList(Constants.ALL_CLASS_TYPES).contains(trimmed);
        if (!match) {
            return "Invalid class type selected. Allowed classes: " + String.join(", ", Constants.ALL_CLASS_TYPES);
        }
        return null;
    }

    /**
     * Validates PNR number format.
     * Must be exactly 10 numeric digits.
     *
     * @param rawPnr PNR string
     * @return null if valid, or an error message string
     */
    public static String validatePnr(String rawPnr) {
        if (rawPnr == null || rawPnr.trim().isEmpty()) {
            return "PNR number cannot be empty.";
        }
        String trimmed = rawPnr.trim();
        if (!PNR_PATTERN.matcher(trimmed).matches()) {
            return "PNR must be exactly " + Constants.PNR_LENGTH + " digits.";
        }
        return null;
    }

    /**
     * Comprehensive reservation validation aggregating all field checks into a {@link ValidationResult}.
     *
     * @param passengerName passenger name
     * @param trainNumber   train number
     * @param classType     class type
     * @param journeyDate   journey date
     * @param source        source station
     * @param destination   destination station
     * @return ValidationResult object
     */
    public static ValidationResult validateReservation(String passengerName, String trainNumber,
                                                       String classType, String journeyDate,
                                                       String source, String destination) {
        ValidationResult result = new ValidationResult();

        String errName = validatePassengerName(passengerName);
        if (errName != null) result.addError(errName);

        String errTrain = validateTrainNumberString(trainNumber);
        if (errTrain != null) result.addError(errTrain);

        String errClass = validateClassType(classType);
        if (errClass != null) result.addError(errClass);

        String errDate = validateJourneyDate(journeyDate);
        if (errDate != null) result.addError(errDate);

        String errStations = validateStations(source, destination);
        if (errStations != null) result.addError(errStations);

        return result;
    }
}
