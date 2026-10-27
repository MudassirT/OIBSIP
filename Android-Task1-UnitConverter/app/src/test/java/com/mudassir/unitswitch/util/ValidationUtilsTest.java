package com.mudassir.unitswitch.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.mudassir.unitswitch.logic.Category;
import com.mudassir.unitswitch.logic.UnitModel;

import org.junit.Test;

/**
 * Unit tests for input validation logic (ValidationUtils).
 */
public class ValidationUtilsTest {

    private final UnitModel meter = Category.LENGTH.findUnitById("M");
    private final UnitModel celsius = Category.TEMPERATURE.findUnitById("C");
    private final UnitModel fahrenheit = Category.TEMPERATURE.findUnitById("F");
    private final UnitModel kelvin = Category.TEMPERATURE.findUnitById("K");

    @Test
    public void testEmptyAndNullInput() {
        ValidationResult res1 = ValidationUtils.validateInput("", Category.LENGTH, meter);
        assertFalse(res1.isValid());
        assertTrue(res1.isEmpty());

        ValidationResult res2 = ValidationUtils.validateInput("   ", Category.LENGTH, meter);
        assertFalse(res2.isValid());
        assertTrue(res2.isEmpty());

        ValidationResult res3 = ValidationUtils.validateInput(null, Category.LENGTH, meter);
        assertFalse(res3.isValid());
        assertTrue(res3.isEmpty());
    }

    @Test
    public void testNonNumericInput() {
        String[] invalidInputs = {"-", ".", "-.", "abc", "1.2.3", "1,2,3", "12a", "--5", "++1"};
        for (String input : invalidInputs) {
            ValidationResult res = ValidationUtils.validateInput(input, Category.LENGTH, meter);
            assertFalse("Expected invalid for: " + input, res.isValid());
            assertFalse(res.isEmpty());
        }
    }

    @Test
    public void testValidNegativeAndDecimalInputs() {
        ValidationResult res1 = ValidationUtils.validateInput("-5.5", Category.TEMPERATURE, celsius);
        assertTrue(res1.isValid());
        assertEquals(-5.5, res1.getValue(), 1e-6);

        ValidationResult res2 = ValidationUtils.validateInput("100.25", Category.LENGTH, meter);
        assertTrue(res2.isValid());
        assertEquals(100.25, res2.getValue(), 1e-6);
    }

    @Test
    public void testCommaDecimalInput_EuropeanLocale() {
        ValidationResult res = ValidationUtils.validateInput("5,5", Category.LENGTH, meter);
        assertTrue(res.isValid());
        assertEquals(5.5, res.getValue(), 1e-6);
    }

    @Test
    public void testAbsoluteZeroValidation() {
        // -300 C is below absolute zero
        ValidationResult resC = ValidationUtils.validateInput("-300", Category.TEMPERATURE, celsius);
        assertFalse(resC.isValid());
        assertTrue(resC.isBelowAbsoluteZero());

        // -500 F is below absolute zero
        ValidationResult resF = ValidationUtils.validateInput("-500", Category.TEMPERATURE, fahrenheit);
        assertFalse(resF.isValid());
        assertTrue(resF.isBelowAbsoluteZero());

        // -1 K is below absolute zero
        ValidationResult resK = ValidationUtils.validateInput("-1", Category.TEMPERATURE, kelvin);
        assertFalse(resK.isValid());
        assertTrue(resK.isBelowAbsoluteZero());

        // Exactly 0 K is valid
        ValidationResult res0K = ValidationUtils.validateInput("0", Category.TEMPERATURE, kelvin);
        assertTrue(res0K.isValid());
    }
}
