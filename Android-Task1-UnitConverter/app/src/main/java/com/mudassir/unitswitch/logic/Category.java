package com.mudassir.unitswitch.logic;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.mudassir.unitswitch.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration of all measurement categories supported in UnitSwitch.
 * Each category specifies its localized name, icon, base unit symbol, and supported units.
 */
public enum Category {

    LENGTH(
            R.string.category_length,
            R.drawable.ic_cat_length,
            "m",
            new UnitModel("MM", R.string.unit_millimetre, "mm", 0.001),
            new UnitModel("CM", R.string.unit_centimetre, "cm", 0.01),
            new UnitModel("M", R.string.unit_metre, "m", 1.0),
            new UnitModel("KM", R.string.unit_kilometre, "km", 1000.0),
            new UnitModel("IN", R.string.unit_inch, "in", 0.0254),
            new UnitModel("FT", R.string.unit_foot, "ft", 0.3048),
            new UnitModel("YD", R.string.unit_yard, "yd", 0.9144),
            new UnitModel("MI", R.string.unit_mile, "mi", 1609.344)
    ),

    WEIGHT(
            R.string.category_weight,
            R.drawable.ic_cat_weight,
            "kg",
            new UnitModel("MG", R.string.unit_milligram, "mg", 0.000001),
            new UnitModel("G", R.string.unit_gram, "g", 0.001),
            new UnitModel("KG", R.string.unit_kilogram, "kg", 1.0),
            new UnitModel("T", R.string.unit_tonne, "t", 1000.0),
            new UnitModel("OZ", R.string.unit_ounce, "oz", 0.028349523125),
            new UnitModel("LB", R.string.unit_pound, "lb", 0.45359237)
    ),

    TEMPERATURE(
            R.string.category_temperature,
            R.drawable.ic_cat_temperature,
            "°C",
            new UnitModel("C", R.string.unit_celsius, "°C", 1.0),
            new UnitModel("F", R.string.unit_fahrenheit, "°F", 1.0),
            new UnitModel("K", R.string.unit_kelvin, "K", 1.0)
    ),

    VOLUME(
            R.string.category_volume,
            R.drawable.ic_cat_volume,
            "L",
            new UnitModel("ML", R.string.unit_millilitre, "mL", 0.001),
            new UnitModel("L", R.string.unit_litre, "L", 1.0),
            new UnitModel("M3", R.string.unit_cubic_metre, "m³", 1000.0),
            new UnitModel("TSP", R.string.unit_teaspoon_us, "tsp", 3.785411784 / 768.0),
            new UnitModel("TBSP", R.string.unit_tablespoon_us, "tbsp", 3.785411784 / 256.0),
            new UnitModel("FLOZ", R.string.unit_fluid_ounce_us, "fl oz", 3.785411784 / 128.0),
            new UnitModel("CUP", R.string.unit_cup_us, "cup", 3.785411784 / 16.0),
            new UnitModel("GAL", R.string.unit_gallon_us, "gal", 3.785411784)
    ),

    SPEED(
            R.string.category_speed,
            R.drawable.ic_cat_speed,
            "m/s",
            new UnitModel("MPS", R.string.unit_mps, "m/s", 1.0),
            new UnitModel("KMPH", R.string.unit_kmph, "km/h", 1.0 / 3.6),
            new UnitModel("MPH", R.string.unit_mph, "mph", 0.44704),
            new UnitModel("KNOT", R.string.unit_knot, "kn", 1852.0 / 3600.0)
    );

    private final int nameResId;
    private final int iconResId;
    private final String baseUnitSymbol;
    private final List<UnitModel> units;

    Category(@StringRes int nameResId, @DrawableRes int iconResId, @NonNull String baseUnitSymbol, UnitModel... units) {
        this.nameResId = nameResId;
        this.iconResId = iconResId;
        this.baseUnitSymbol = baseUnitSymbol;
        List<UnitModel> list = new ArrayList<>();
        Collections.addAll(list, units);
        this.units = Collections.unmodifiableList(list);
    }

    @StringRes
    public int getNameResId() {
        return nameResId;
    }

    @DrawableRes
    public int getIconResId() {
        return iconResId;
    }

    @NonNull
    public String getBaseUnitSymbol() {
        return baseUnitSymbol;
    }

    @NonNull
    public List<UnitModel> getUnits() {
        return units;
    }

    @Nullable
    public UnitModel findUnitById(@NonNull String unitId) {
        for (UnitModel unit : units) {
            if (unit.getId().equalsIgnoreCase(unitId)) {
                return unit;
            }
        }
        return null;
    }

    public boolean isTemperature() {
        return this == TEMPERATURE;
    }
}
