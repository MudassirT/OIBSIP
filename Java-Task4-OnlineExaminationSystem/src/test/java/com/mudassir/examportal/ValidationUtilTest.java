package com.mudassir.examportal;

import com.mudassir.examportal.service.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validation Utilities Tests")
class ValidationUtilTest {

    @Test
    @DisplayName("Should accept valid display names and reject invalid characters or lengths")
    void testDisplayNameValidation() {
        // Valid display names (2-40 chars, letters, spaces, dots, hyphens)
        assertTrue(ValidationUtil.isValidDisplayName("Mudassir Ahmed"));
        assertTrue(ValidationUtil.isValidDisplayName("John D."));
        assertTrue(ValidationUtil.isValidDisplayName("Mary-Jane Watson"));
        assertTrue(ValidationUtil.isValidDisplayName("Dr. Smith"));
        assertTrue(ValidationUtil.isValidDisplayName("Al"));

        // Invalid display names
        assertFalse(ValidationUtil.isValidDisplayName(null), "Null name must be invalid");
        assertFalse(ValidationUtil.isValidDisplayName(""), "Empty name must be invalid");
        assertFalse(ValidationUtil.isValidDisplayName("   "), "Whitespace name must be invalid");
        assertFalse(ValidationUtil.isValidDisplayName("A"), "Single character name must be invalid (min 2)");
        assertFalse(ValidationUtil.isValidDisplayName("User123"), "Numbers should not be allowed in display name");
        assertFalse(ValidationUtil.isValidDisplayName("Hacker<script>"), "HTML/Script tags must be rejected");
        assertFalse(ValidationUtil.isValidDisplayName("Name_With_Underscore"), "Underscores not allowed");
        assertFalse(ValidationUtil.isValidDisplayName("This display name is definitely far too long because it exceeds forty characters limit"));
    }

    @Test
    @DisplayName("Should enforce password complexity: min 8 chars with at least one letter and one number")
    void testPasswordComplexityValidation() {
        // Valid passwords
        assertTrue(ValidationUtil.isValidNewPassword("Password123"));
        assertTrue(ValidationUtil.isValidNewPassword("Pass12345678"));
        assertTrue(ValidationUtil.isValidNewPassword("Abcd5678"));
        assertTrue(ValidationUtil.isValidNewPassword("Student@123"));

        // Invalid passwords
        assertFalse(ValidationUtil.isValidNewPassword((String) null), "Null password must be invalid");
        assertFalse(ValidationUtil.isValidNewPassword((char[]) null), "Null password char[] must be invalid");
        assertFalse(ValidationUtil.isValidNewPassword(""), "Empty password must be invalid");
        assertFalse(ValidationUtil.isValidNewPassword("Short1"), "Less than 8 chars must be invalid");
        assertFalse(ValidationUtil.isValidNewPassword("12345678"), "Only digits must be invalid (needs letters)");
        assertFalse(ValidationUtil.isValidNewPassword("abcdefgh"), "Only letters must be invalid (needs digits)");
    }
}
