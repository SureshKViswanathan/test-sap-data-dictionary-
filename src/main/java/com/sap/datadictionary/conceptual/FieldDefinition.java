package com.sap.datadictionary.conceptual;

import com.sap.datadictionary.internal.DataElement;

/**
 * Represents a single field within a {@link TableDefinition} or {@link Structure}.
 * <p>
 * In the Conceptual Schema, a field definition describes a logical column:
 * its position, whether it is a key field, and a reference to its
 * {@link DataElement} which supplies both semantic labels and physical
 * type information (via the underlying Domain).
 * </p>
 */
public class FieldDefinition {

    private final String fieldName;
    private final DataElement dataElement;
    private final boolean keyField;
    private final boolean nullable;

    public FieldDefinition(String fieldName, DataElement dataElement,
                           boolean keyField, boolean nullable) {
        if (fieldName == null || fieldName.isBlank()) {
            throw new IllegalArgumentException("Field name must not be blank");
        }
        if (dataElement == null) {
            throw new IllegalArgumentException("Data element must not be null");
        }
        this.fieldName = fieldName;
        this.dataElement = dataElement;
        this.keyField = keyField;
        this.nullable = nullable;
    }

    public String getFieldName() {
        return fieldName;
    }

    public DataElement getDataElement() {
        return dataElement;
    }

    public boolean isKeyField() {
        return keyField;
    }

    public boolean isNullable() {
        return nullable;
    }

    @Override
    public String toString() {
        return "FieldDefinition{name='" + fieldName + "', key=" + keyField
                + ", nullable=" + nullable + ", dataElement=" + dataElement.getName() + '}';
    }
}
