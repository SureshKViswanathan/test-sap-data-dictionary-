package com.sap.datadictionary.conceptual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an SAP Data Dictionary transparent table definition.
 * <p>
 * In the ANSI/SPARC Conceptual Schema, a table is the central logical
 * entity that maps business data into a structured format. Each table
 * has an ordered list of {@link FieldDefinition}s and metadata such as
 * its delivery class and whether data buffering is enabled.
 * </p>
 */
public class TableDefinition {

    /** SAP delivery class (A = application, C = customizing, etc.) */
    public enum DeliveryClass { A, C, L, G, E, S, W }

    private final String tableName;
    private final Map<String, FieldDefinition> fields;
    private String description;
    private DeliveryClass deliveryClass;
    private boolean buffered;

    public TableDefinition(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            throw new IllegalArgumentException("Table name must not be blank");
        }
        this.tableName = tableName;
        this.fields = new LinkedHashMap<>();
        this.deliveryClass = DeliveryClass.A;
    }

    /** Add a field. Duplicate field names are rejected. */
    public void addField(FieldDefinition field) {
        if (field == null) {
            throw new IllegalArgumentException("Field must not be null");
        }
        if (fields.containsKey(field.getFieldName())) {
            throw new IllegalArgumentException(
                    "Duplicate field: " + field.getFieldName());
        }
        fields.put(field.getFieldName(), field);
    }

    public FieldDefinition getField(String fieldName) {
        return fields.get(fieldName);
    }

    /** Return an unmodifiable ordered list of all fields. */
    public List<FieldDefinition> getFields() {
        return Collections.unmodifiableList(new ArrayList<>(fields.values()));
    }

    /** Return only the key fields in their defined order. */
    public List<FieldDefinition> getKeyFields() {
        return fields.values().stream()
                .filter(FieldDefinition::isKeyField)
                .toList();
    }

    public String getTableName() {
        return tableName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DeliveryClass getDeliveryClass() {
        return deliveryClass;
    }

    public void setDeliveryClass(DeliveryClass deliveryClass) {
        this.deliveryClass = deliveryClass;
    }

    public boolean isBuffered() {
        return buffered;
    }

    public void setBuffered(boolean buffered) {
        this.buffered = buffered;
    }

    @Override
    public String toString() {
        return "TableDefinition{name='" + tableName + "', fields=" + fields.size()
                + ", deliveryClass=" + deliveryClass + '}';
    }
}
