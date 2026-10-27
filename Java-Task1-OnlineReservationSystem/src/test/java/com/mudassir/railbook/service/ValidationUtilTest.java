package com.mudassir.railbook.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests validating all business rules, bounds, and date restrictions.
 *
 * @author Mudassir Ahmed
 */
class ValidationUtilTest {

    @Test
    @DisplayName("Passenger name: valid names pass, invalid names are rejected")
    void testValidatePassengerName() {
        assertNull(ValidationUtil.validatePassengerName("Muhammad Mudassir"));
        assertNull(ValidationUtil.validatePassengerName("A. B. Khan"));
        assertNull(ValidationUtil.validatePassengerName("Mary-Jane O'Connor"));

        // Invalid names
        assertNotNull(ValidationUtil.validatePassengerName(""), "Empty name should be rejected");
        assertNotNull(ValidationUtil.validatePassengerName("   "), "Blank spaces should be rejected");
        assertNotNull(ValidationUtil.validatePassengerName("A"), "Single character should be rejected");
        assertNotNull(ValidationUtil.validatePassengerName("A".repeat(61)), "Names > 60 chars should be rejected");
        assertNotNull(ValidationUtil.validatePassengerName("User123"), "Numbers should be rejected");
        assertNotNull(ValidationUtil.validatePassengerName("John<script>"), "HTML/Script tags should be rejected");
    }

    @Test
    @DisplayName("Train number: numeric positive values pass, non-numeric values are rejected")
    void testValidateTrainNumber() {
        assertNull(ValidationUtil.validateTrainNumberString("101"));
        assertNull(ValidationUtil.validateTrainNumberString(" 105 "));

        // Invalid train numbers
        assertNotNull(ValidationUtil.validateTrainNumberString(""), "Empty train number should be rejected");
        assertNotNull(ValidationUtil.validateTrainNumberString("abc"), "Alphabetical input should be rejected");
        assertNotNull(ValidationUtil.validateTrainNumberString("-5"), "Negative number should be rejected");
        assertNotNull(ValidationUtil.validateTrainNumberString("0"), "Zero should be rejected");
        assertNotNull(ValidationUtil.validateTrainNumberString("101.5"), "Decimal should be rejected");
    }

    @Test
    @DisplayName("Journey date: valid upcoming dates pass; past, invalid calendar, and distant dates are rejected")
    void testValidateJourneyDate() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        assertNull(ValidationUtil.validateJourneyDate(tomorrow.toString()));

        LocalDate in30Days = LocalDate.now().plusDays(30);
        assertNull(ValidationUtil.validateJourneyDate(in30Days.toString()));

        // Past date
        LocalDate yesterday = LocalDate.now().minusDays(1);
        assertNotNull(ValidationUtil.validateJourneyDate(yesterday.toString()), "Past date must be rejected");

        // More than 120 days ahead
        LocalDate in150Days = LocalDate.now().plusDays(150);
        assertNotNull(ValidationUtil.validateJourneyDate(in150Days.toString()), "Date > 120 days ahead must be rejected");

        // Impossible calendar dates
        assertNotNull(ValidationUtil.validateJourneyDate("2026-02-30"), "February 30 must be rejected");
        assertNotNull(ValidationUtil.validateJourneyDate("2026-04-31"), "April 31 must be rejected");
        assertNotNull(ValidationUtil.validateJourneyDate("invalid-date"), "Invalid format must be rejected");
        assertNotNull(ValidationUtil.validateJourneyDate("2026/10/20"), "Slash format must be rejected");
    }

    @Test
    @DisplayName("Stations: different source and destination pass, identical or empty fail")
    void testValidateStations() {
        assertNull(ValidationUtil.validateStations("Karachi Cantt", "Lahore Junction"));
        assertNull(ValidationUtil.validateStations("Rawalpindi", "Islamabad"));

        assertNotNull(ValidationUtil.validateStations("Karachi Cantt", "Karachi Cantt"),
                "Identical source and destination must be rejected");
        assertNotNull(ValidationUtil.validateStations("Lahore Junction", "lahore junction"),
                "Case-insensitive identical stations must be rejected");
        assertNotNull(ValidationUtil.validateStations("", "Lahore Junction"),
                "Empty source must be rejected");
        assertNotNull(ValidationUtil.validateStations("Karachi Cantt", "  "),
                "Empty destination must be rejected");
    }

    @Test
    @DisplayName("PNR: exactly 10 digits pass, other formats fail")
    void testValidatePnr() {
        assertNull(ValidationUtil.validatePnr("1234567890"));
        assertNull(ValidationUtil.validatePnr("9876543210"));

        assertNotNull(ValidationUtil.validatePnr("123456789"), "9 digits must be rejected");
        assertNotNull(ValidationUtil.validatePnr("12345678901"), "11 digits must be rejected");
        assertNotNull(ValidationUtil.validatePnr("12345ABCDE"), "Alphanumeric must be rejected");
        assertNotNull(ValidationUtil.validatePnr(""), "Empty PNR must be rejected");
        assertNotNull(ValidationUtil.validatePnr("          "), "Whitespace PNR must be rejected");
    }

    @Test
    @DisplayName("Comprehensive reservation validation aggregates all field checks")
    void testValidateReservationComprehensive() {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        ValidationResult validResult = ValidationUtil.validateReservation(
                "Mudassir Ahmed", "101", "AC First", tomorrow, "Karachi Cantt", "Islamabad");
        assertTrue(validResult.isValid(), "All valid fields must pass validation");

        ValidationResult invalidResult = ValidationUtil.validateReservation(
                "X", "abc", "Unknown Class", "2026-02-30", "Karachi Cantt", "Karachi Cantt");
        assertFalse(invalidResult.isValid(), "Invalid fields must fail validation");
        assertTrue(invalidResult.getErrors().size() >= 4, "Should report multiple validation errors");
    }
}
