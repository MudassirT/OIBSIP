package com.mudassir.taskvault;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.mudassir.taskvault.util.Validators;

import org.junit.Test;

/**
 * Unit tests validating input validation routines for users and tasks.
 */
public class ValidatorsTest {

    @Test
    public void testValidName() {
        assertTrue(Validators.isValidName("Mudassir Ahmed"));
        assertTrue(Validators.isValidName("A"));
        assertFalse(Validators.isValidName(""));
        assertFalse(Validators.isValidName("   "));
        assertFalse(Validators.isValidName(null));
    }

    @Test
    public void testValidEmail() {
        assertTrue(Validators.isValidEmail("user@example.com"));
        assertTrue(Validators.isValidEmail("mudassir.ahmed@domain.org"));
        assertTrue(Validators.isValidEmail("test+tag@company.co.uk"));

        assertFalse(Validators.isValidEmail("not-an-email"));
        assertFalse(Validators.isValidEmail("user@"));
        assertFalse(Validators.isValidEmail("@domain.com"));
        assertFalse(Validators.isValidEmail("user@domain"));
        assertFalse(Validators.isValidEmail(""));
        assertFalse(Validators.isValidEmail(null));
    }

    @Test
    public void testPasswordLength() {
        assertTrue(Validators.isPasswordLengthValid("12345678")); // 8 chars
        assertTrue(Validators.isPasswordLengthValid("StrongPassword123"));
        assertFalse(Validators.isPasswordLengthValid("short1")); // 6 chars
        assertFalse(Validators.isPasswordLengthValid(""));
        assertFalse(Validators.isPasswordLengthValid(null));
    }

    @Test
    public void testPasswordContainsDigit() {
        assertTrue(Validators.doesPasswordContainDigit("password1"));
        assertTrue(Validators.doesPasswordContainDigit("9passwords"));
        assertTrue(Validators.doesPasswordContainDigit("p@ssw0rd"));
        assertFalse(Validators.doesPasswordContainDigit("password"));
        assertFalse(Validators.doesPasswordContainDigit("JustLettersOnly!"));
        assertFalse(Validators.doesPasswordContainDigit(null));
    }

    @Test
    public void testPasswordsMatch() {
        assertTrue(Validators.doPasswordsMatch("Secret123", "Secret123"));
        assertFalse(Validators.doPasswordsMatch("Secret123", "Secret124"));
        assertFalse(Validators.doPasswordsMatch("Secret123", ""));
        assertFalse(Validators.doPasswordsMatch(null, "Secret123"));
        assertFalse(Validators.doPasswordsMatch("Secret123", null));
    }

    @Test
    public void testValidTaskTitle() {
        assertTrue(Validators.isValidTaskTitle("Buy groceries"));
        assertTrue(Validators.isValidTaskTitle("Submit Oasis Infobyte Task 2"));
        assertFalse(Validators.isValidTaskTitle(""));
        assertFalse(Validators.isValidTaskTitle("   "));
        assertFalse(Validators.isValidTaskTitle(null));

        // Exactly 100 characters should be valid
        StringBuilder sb100 = new StringBuilder();
        for (int i = 0; i < 100; i++) sb100.append("a");
        assertTrue(Validators.isValidTaskTitle(sb100.toString()));

        // 101 characters should be rejected
        StringBuilder sb101 = new StringBuilder();
        for (int i = 0; i < 101; i++) sb101.append("a");
        assertFalse(Validators.isValidTaskTitle(sb101.toString()));
    }

    @Test
    public void testTaskNotesLength() {
        assertTrue(Validators.areTaskNotesValid(null));
        assertTrue(Validators.areTaskNotesValid(""));
        assertTrue(Validators.areTaskNotesValid("Short note"));

        StringBuilder sb500 = new StringBuilder();
        for (int i = 0; i < 500; i++) sb500.append("x");
        assertTrue(Validators.areTaskNotesValid(sb500.toString()));

        StringBuilder sb501 = new StringBuilder();
        for (int i = 0; i < 501; i++) sb501.append("x");
        assertFalse(Validators.areTaskNotesValid(sb501.toString()));
    }
}
