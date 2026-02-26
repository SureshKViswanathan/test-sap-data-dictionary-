package com.sap.datadictionary.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValueRangeTest {

    @Test
    void emptyValueRangeAcceptsAnything() {
        ValueRange range = new ValueRange();

        assertTrue(range.isValid("anything"));
        assertTrue(range.getFixedValues().isEmpty());
    }

    @Test
    void fixedValuesRestrictInput() {
        ValueRange range = new ValueRange();
        range.addFixedValue("EUR");
        range.addFixedValue("USD");

        assertTrue(range.isValid("EUR"));
        assertTrue(range.isValid("USD"));
        assertFalse(range.isValid("GBP"));
    }

    @Test
    void fixedValuesAreUnmodifiable() {
        ValueRange range = new ValueRange();
        range.addFixedValue("X");

        assertThrows(UnsupportedOperationException.class,
                () -> range.getFixedValues().add("Y"));
    }

    @Test
    void nullFixedValueIsRejected() {
        ValueRange range = new ValueRange();

        assertThrows(IllegalArgumentException.class,
                () -> range.addFixedValue(null));
    }
}
