package com.mudassir.unitswitch.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for formatting logic (FormatUtils).
 */
public class FormatUtilsTest {

    @Test
    public void testFormatNumber_basicAndDecimals() {
        assertEquals("1", FormatUtils.formatNumber(1.0));
        assertEquals("0.5", FormatUtils.formatNumber(0.5));
        assertEquals("1000", FormatUtils.formatNumber(1000.0));
        assertEquals("0", FormatUtils.formatNumber(0.0));
        assertEquals("0", FormatUtils.formatNumber(-0.0));
    }

    @Test
    public void testFormatNumber_roundingTo6Decimals() {
        assertEquals("0.621371", FormatUtils.formatNumber(0.621371192));
        assertEquals("2.204623", FormatUtils.formatNumber(2.20462262));
        assertEquals("3.785412", FormatUtils.formatNumber(3.785411784));
        assertEquals("1.609344", FormatUtils.formatNumber(1.609344));
    }

    @Test
    public void testFormatEquation() {
        String eq = FormatUtils.formatEquation(100.0, "cm", 1.0, "m");
        assertEquals("100 cm = 1 m", eq);

        String eq2 = FormatUtils.formatEquation(100.0, "cm", 39.3700787, "in");
        assertEquals("100 cm = 39.370079 in", eq2);
    }

    @Test
    public void testFormatNumber_scientificNotationForExtremes() {
        assertEquals("1E13", FormatUtils.formatNumber(1e13));
        assertEquals("1E-7", FormatUtils.formatNumber(1e-7));
    }
}
