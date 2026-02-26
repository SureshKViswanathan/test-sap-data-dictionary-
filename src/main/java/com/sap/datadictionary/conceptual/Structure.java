package com.sap.datadictionary.conceptual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an SAP Data Dictionary Structure.
 * <p>
 * A Structure is similar to a {@link TableDefinition} but has no
 * corresponding database table. Structures are used in the Conceptual
 * Schema to model complex parameters, interface types, and work areas.
 * </p>
 */
public class Structure {

    private final String structureName;
    private final Map<String, FieldDefinition> fields;
    private String description;

    public Structure(String structureName) {
        if (structureName == null || structureName.isBlank()) {
            throw new IllegalArgumentException("Structure name must not be blank");
        }
        this.structureName = structureName;
        this.fields = new LinkedHashMap<>();
    }

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

    public List<FieldDefinition> getFields() {
        return Collections.unmodifiableList(new ArrayList<>(fields.values()));
    }

    public String getStructureName() {
        return structureName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Structure{name='" + structureName + "', fields=" + fields.size() + '}';
    }
}
