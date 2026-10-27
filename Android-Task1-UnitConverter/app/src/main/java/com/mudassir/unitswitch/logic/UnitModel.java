package com.mudassir.unitswitch.logic;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.Objects;

/**
 * Immutable model representing a single unit of measurement.
 */
public class UnitModel {

    private final String id;
    private final int nameResId;
    private final String symbol;
    private final double factorToBase;

    /**
     * @param id           Unique unit identifier (e.g., "METER")
     * @param nameResId    Resource ID for user-facing localized name
     * @param symbol       Standard unit symbol (e.g., "m")
     * @param factorToBase Conversion multiplier to base unit (e.g., 0.001 for mm -> m)
     */
    public UnitModel(@NonNull String id, @StringRes int nameResId, @NonNull String symbol, double factorToBase) {
        this.id = id;
        this.nameResId = nameResId;
        this.symbol = symbol;
        this.factorToBase = factorToBase;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @StringRes
    public int getNameResId() {
        return nameResId;
    }

    @NonNull
    public String getSymbol() {
        return symbol;
    }

    public double getFactorToBase() {
        return factorToBase;
    }

    @NonNull
    @Override
    public String toString() {
        return symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnitModel unitModel = (UnitModel) o;
        return Objects.equals(id, unitModel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
