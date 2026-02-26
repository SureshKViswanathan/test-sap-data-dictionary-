package com.sap.datadictionary.external;

import com.sap.datadictionary.conceptual.TableDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an SAP Data Dictionary Search Help.
 * <p>
 * Search Helps belong to the External Schema because they define how
 * end-users look up and select values. An elementary search help
 * references a single table and specifies which fields to display and
 * which to export back to the calling screen.
 * </p>
 */
public class SearchHelp {

    private final String name;
    private TableDefinition selectionMethod;
    private final List<String> displayFields;
    private final List<String> exportFields;
    private String description;

    public SearchHelp(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Search help name must not be blank");
        }
        this.name = name;
        this.displayFields = new ArrayList<>();
        this.exportFields = new ArrayList<>();
    }

    public void setSelectionMethod(TableDefinition table) {
        this.selectionMethod = table;
    }

    public void addDisplayField(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            throw new IllegalArgumentException("Display field name must not be blank");
        }
        displayFields.add(fieldName);
    }

    public void addExportField(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            throw new IllegalArgumentException("Export field name must not be blank");
        }
        exportFields.add(fieldName);
    }

    public String getName() {
        return name;
    }

    public TableDefinition getSelectionMethod() {
        return selectionMethod;
    }

    public List<String> getDisplayFields() {
        return Collections.unmodifiableList(displayFields);
    }

    public List<String> getExportFields() {
        return Collections.unmodifiableList(exportFields);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "SearchHelp{name='" + name + "', selectionMethod="
                + (selectionMethod != null ? selectionMethod.getTableName() : "null") + '}';
    }
}
