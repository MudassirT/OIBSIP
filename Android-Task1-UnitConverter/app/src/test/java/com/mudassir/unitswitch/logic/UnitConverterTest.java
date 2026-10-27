package com.mudassir.unitswitch.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.mudassir.unitswitch.util.FormatUtils;

import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit test suite for UnitConverter.
 * Validates all required conversions, linear base-unit ratios,
 * non-linear temperature formulas, and physical absolute-zero thresholds.
 */
public class UnitConverterTest {

    private UnitConverter converter;

    @Before
    public void setUp() {
        converter = new UnitConverter();
    }

    // ==================== LENGTH TESTS ====================

    @Test
    public void testLength_100cmToM() {
        UnitModel cm = Category.LENGTH.findUnitById("CM");
        UnitModel m = Category.LENGTH.findUnitById("M");
        ConversionResult result = converter.convert(Category.LENGTH, cm, m, 100.0);
        assertTrue(result.isSuccess());
        assertEquals("1", FormatUtils.formatNumber(result.getResultValue()));
    }

    @Test
    public void testLength_1kmToMile() {
        UnitModel km = Category.LENGTH.findUnitById("KM");
        UnitModel mi = Category.LENGTH.findUnitById("MI");
        ConversionResult result = converter.convert(Category.LENGTH, km, mi, 1.0);
        assertTrue(result.isSuccess());
        assertEquals("0.621371", FormatUtils.formatNumber(result.getResultValue()));
    }

    @Test
    public void testLength_12inToFt() {
        UnitModel in = Category.LENGTH.findUnitById("IN");
        UnitModel ft = Category.LENGTH.findUnitById("FT");
        ConversionResult result = converter.convert(Category.LENGTH, in, ft, 12.0);
        assertTrue(result.isSuccess());
        assertEquals("1", FormatUtils.formatNumber(result.getResultValue()));
    }

    @Test
    public void testLength_1mileToKm() {
        UnitModel mi = Category.LENGTH.findUnitById("MI");
        UnitModel km = Category.LENGTH.findUnitById("KM");
        ConversionResult result = converter.convert(Category.LENGTH, mi, km, 1.0);
        assertTrue(result.isSuccess());
        assertEquals("1.609344", FormatUtils.formatNumber(result.getResultValue()));
    }

    // ==================== WEIGHT TESTS ====================

    @Test
    public void testWeight_1kgToLb() {
        UnitModel kg = Category.WEIGHT.findUnitById("KG");
        UnitModel lb = Category.WEIGHT.findUnitById("LB");
        ConversionResult result = converter.convert(Category.WEIGHT, kg, lb, 1.0);
        assertTrue(result.isSuccess());
        assertEquals("2.204623", FormatUtils.formatNumber(result.getResultValue()));
    }

    @Test
    public void testWeight_500gToKg() {
        UnitModel g = Category.WEIGHT.findUnitById("G");
        UnitModel kg = Category.WEIGHT.findUnitById("KG");
        ConversionResult result = converter.convert(Category.WEIGHT, g, kg, 500.0);
        assertTrue(result.isSuccess());
        assertEquals("0.5", FormatUtils.formatNumber(result.getResultValue()));
    }

    @Test
    public void testWeight_1lbToOz() {
        UnitModel lb = Category.WEIGHT.findUnitById("LB");
        UnitModel oz = Category.WEIGHT.findUnitById("OZ");
        ConversionResult result = converter.convert(Category.WEIGHT, lb, oz, 1.0);
        assertTrue(result.isSuccess());
        assertEquals("16", FormatUtils.formatNumber(result.getResultValue()));
    }

    // ==================== TEMPERATURE TESTS ====================

    @Test
    public void testTemperature_0CToF() {
        UnitModel c = Category.TEMPERATURE.findUnitById("C");
        UnitModel f = Category.TEMPERATURE.findUnitById("F");
        ConversionResult result = converter.convert(Category.TEMPERATURE, c, f, 0.0);
        assertTrue(result.isSuccess());
        assertEquals("32", FormatUtils.formatNumber(result.getResultValue()));
    }

    @Test
    public void testTemperature_100CToF() {
        UnitModel c = Category.TEMPERATURE.findUnitById("C");
        UnitModel f = Category.TEMPERATURE.findUnitById("F");
        ConversionResult result = converter.convert(Category.TEMPERATURE, c, f, 100.0);
        assertTrue(result.isSuccess());
        assertEquals("212", FormatUtils.formatNumber(result.getResultValue()));
    }

    @Test
    public void testTemperature_32FToC() {
        UnitModel f = Category.TEMPERATURE.findUnitById("F");
        UnitModel c = Category.TEMPERATURE.findUnitById("C");
        ConversionResult result = converter.convert(Category.TEMPERATURE, f, c, 32.0);
        assertTrue(result.isSuccess());
        assertEquals("0", FormatUtils.formatNumber(result.getResultValue()));
    }

    @Test
    public void testTemperature_0CToK() {
        UnitModel c = Category.TEMPERATURE.findUnitById("C");
        UnitModel k = Category.TEMPERATURE.findUnitById("K");
        ConversionResult result = converter.convert(Category.TEMPERATURE, c, k, 0.0);
        assertTrue(result.isSuccess());
        assertEquals("273.15", FormatUtils.formatNumber(result.getResultValue()));
    }

    @Test
    public void testTemperature_0KToC() {
        UnitModel k = Category.TEMPERATURE.findUnitById("K");
        UnitModel c = Category.TEMPERATURE.findUnitById("C");
        ConversionResult result = converter.convert(Category.TEMPERATURE, k, c, 0.0);
        assertTrue(result.isSuccess());
        assertEquals("-273.15", FormatUtils.formatNumber(result.getResultValue()));
    }

    @Test
    public void testTemperature_minus40CToF() {
        UnitModel c = Category.TEMPERATURE.findUnitById("C");
        UnitModel f = Category.TEMPERATURE.findUnitById("F");
        ConversionResult result = converter.convert(Category.TEMPERATURE, c, f, -40.0);
        assertTrue(result.isSuccess());
        assertEquals("-40", FormatUtils.formatNumber(result.getResultValue()));
    }

    @Test
    public void testTemperature_rejectedBelowAbsoluteZero() {
        UnitModel c = Category.TEMPERATURE.findUnitById("C");
        UnitModel f = Category.TEMPERATURE.findUnitById("F");
        UnitModel k = Category.TEMPERATURE.findUnitById("K");

        // -300 C < -273.15 C
        ConversionResult resC = converter.convert(Category.TEMPERATURE, c, f, -300.0);
        assertFalse(resC.isSuccess());
        assertTrue(resC.isBelowAbsoluteZero());

        // -500 F < -459.67 F
        ConversionResult resF = converter.convert(Category.TEMPERATURE, f, c, -500.0);
        assertFalse(resF.isSuccess());
        assertTrue(resF.isBelowAbsoluteZero());

        // -1 K < 0 K
        ConversionResult resK = converter.convert(Category.TEMPERATURE, k, c, -1.0);
        assertFalse(resK.isSuccess());
        assertTrue(resK.isBelowAbsoluteZero());
    }

    // ==================== VOLUME TESTS ====================

    @Test
    public void testVolume_1GallonToL() {
        UnitModel gal = Category.VOLUME.findUnitById("GAL");
        UnitModel l = Category.VOLUME.findUnitById("L");
        ConversionResult result = converter.convert(Category.VOLUME, gal, l, 1.0);
        assertTrue(result.isSuccess());
        assertEquals("3.785412", FormatUtils.formatNumber(result.getResultValue()));
    }

    @Test
    public void testVolume_1LToMl() {
        UnitModel l = Category.VOLUME.findUnitById("L");
        UnitModel ml = Category.VOLUME.findUnitById("ML");
        ConversionResult result = converter.convert(Category.VOLUME, l, ml, 1.0);
        assertTrue(result.isSuccess());
        assertEquals("1000", FormatUtils.formatNumber(result.getResultValue()));
    }

    // ==================== SPEED TESTS ====================

    @Test
    public void testSpeed_36KmphToMps() {
        UnitModel kmph = Category.SPEED.findUnitById("KMPH");
        UnitModel mps = Category.SPEED.findUnitById("MPS");
        ConversionResult result = converter.convert(Category.SPEED, kmph, mps, 36.0);
        assertTrue(result.isSuccess());
        assertEquals("10", FormatUtils.formatNumber(result.getResultValue()));
    }

    @Test
    public void testSpeed_1KnotToKmph() {
        UnitModel kn = Category.SPEED.findUnitById("KNOT");
        UnitModel kmph = Category.SPEED.findUnitById("KMPH");
        ConversionResult result = converter.convert(Category.SPEED, kn, kmph, 1.0);
        assertTrue(result.isSuccess());
        assertEquals("1.852", FormatUtils.formatNumber(result.getResultValue()));
    }

    // ==================== IDENTITY TESTS ====================

    @Test
    public void testSameUnitConversion_returnsIdenticalValue() {
        UnitModel m = Category.LENGTH.findUnitById("M");
        ConversionResult result = converter.convert(Category.LENGTH, m, m, 42.195);
        assertTrue(result.isSuccess());
        assertEquals("42.195", FormatUtils.formatNumber(result.getResultValue()));
    }
}
