package com.sap.datadictionary.internal;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Defines an allowed value range for a {@link Domain}.
 * <p>
 * A value range constrains the values that may be stored in fields
 * backed by the domain. This is part of the Internal Schema because
 * it directly affects data validation at the storage level.
 * </p>
 */
public class ValueRange {

    private final Set<String> fixedValues;

    public ValueRange() {
        this.fixedValues = new LinkedHashSet<>();
    }

    /** Add a single allowed value. */
    public void addFixedValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Fixed value must not be null");
        }
        fixedValues.add(value);
    }

    /** Return an unmodifiable view of the fixed values. */
    public Set<String> getFixedValues() {
        return Collections.unmodifiableSet(fixedValues);
    }

    /** Check whether a given value is within the allowed range. */
    public boolean isValid(String value) {
        if (fixedValues.isEmpty()) {
            return true; // no restriction
        }
        return fixedValues.contains(value);
    }

    @Override
    public String toString() {
        return "ValueRange{fixedValues=" + fixedValues + '}';
    }
}
