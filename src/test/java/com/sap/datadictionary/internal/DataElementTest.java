package com.sap.datadictionary.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataElementTest {

    private Domain sampleDomain() {
        return new Domain("ZCHAR10", DataType.CHAR, 10);
    }

    @Test
    void createDataElement() {
        DataElement element = new DataElement("ZNAME", sampleDomain());

        assertEquals("ZNAME", element.getName());
        assertEquals("ZCHAR10", element.getDomain().getName());
    }

    @Test
    void labelsCanBeSet() {
        DataElement element = new DataElement("ZNAME", sampleDomain());
        element.setShortLabel("Name");
        element.setMediumLabel("Person Name");
        element.setLongLabel("Full Person Name");

        assertEquals("Name", element.getShortLabel());
        assertEquals("Person Name", element.getMediumLabel());
        assertEquals("Full Person Name", element.getLongLabel());
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new DataElement("", sampleDomain()));
    }

    @Test
    void rejectsNullDomain() {
        assertThrows(IllegalArgumentException.class,
                () -> new DataElement("ZNAME", null));
    }
}
