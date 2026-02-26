package com.sap.datadictionary.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainTest {

    @Test
    void createDomainWithValidAttributes() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);

        assertEquals("ZCHAR10", domain.getName());
        assertEquals(DataType.CHAR, domain.getDataType());
        assertEquals(10, domain.getLength());
        assertEquals(0, domain.getDecimals());
    }

    @Test
    void createDomainWithDecimals() {
        Domain domain = new Domain("ZAMOUNT", DataType.DECIMAL, 15, 2);

        assertEquals(DataType.DECIMAL, domain.getDataType());
        assertEquals(15, domain.getLength());
        assertEquals(2, domain.getDecimals());
    }

    @Test
    void domainRejectsBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Domain("", DataType.CHAR, 10));
    }

    @Test
    void domainRejectsNullDataType() {
        assertThrows(IllegalArgumentException.class,
                () -> new Domain("TEST", null, 10));
    }

    @Test
    void domainRejectsNonPositiveLength() {
        assertThrows(IllegalArgumentException.class,
                () -> new Domain("TEST", DataType.CHAR, 0));
    }

    @Test
    void domainRejectsNegativeDecimals() {
        assertThrows(IllegalArgumentException.class,
                () -> new Domain("TEST", DataType.DECIMAL, 10, -1));
    }

    @Test
    void descriptionCanBeSet() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        domain.setDescription("Ten character text field");

        assertEquals("Ten character text field", domain.getDescription());
    }
}
