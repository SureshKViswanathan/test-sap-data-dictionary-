package com.sap.datadictionary.external;

import com.sap.datadictionary.conceptual.TableDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an SAP Data Dictionary View.
 * <p>
 * In the ANSI/SPARC External Schema, views provide user-specific
 * perspectives of the data. A view references one or more
 * {@link TableDefinition}s and exposes a subset of their fields,
 * optionally with join conditions and selection criteria.
 * </p>
 */
public class ViewDefinition {

    /** SAP view types. */
    public enum ViewType {
        /** Database view – mapped 1-to-1 to an SQL view */
        DATABASE,
        /** Projection view – simple column subset of a single table */
        PROJECTION,
        /** Maintenance view – for SM30 data maintenance */
        MAINTENANCE,
        /** Help view – used with search helps */
        HELP
    }

    private final String viewName;
    private final ViewType viewType;
    private final List<TableDefinition> baseTables;
    private final List<String> selectedFields;
    private String description;

    public ViewDefinition(String viewName, ViewType viewType) {
        if (viewName == null || viewName.isBlank()) {
            throw new IllegalArgumentException("View name must not be blank");
        }
        if (viewType == null) {
            throw new IllegalArgumentException("View type must not be null");
        }
        this.viewName = viewName;
        this.viewType = viewType;
        this.baseTables = new ArrayList<>();
        this.selectedFields = new ArrayList<>();
    }

    public void addBaseTable(TableDefinition table) {
        if (table == null) {
            throw new IllegalArgumentException("Base table must not be null");
        }
        baseTables.add(table);
    }

    public void addSelectedField(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            throw new IllegalArgumentException("Field name must not be blank");
        }
        selectedFields.add(fieldName);
    }

    public String getViewName() {
        return viewName;
    }

    public ViewType getViewType() {
        return viewType;
    }

    public List<TableDefinition> getBaseTables() {
        return Collections.unmodifiableList(baseTables);
    }

    public List<String> getSelectedFields() {
        return Collections.unmodifiableList(selectedFields);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ViewDefinition{name='" + viewName + "', type=" + viewType
                + ", baseTables=" + baseTables.size() + '}';
    }
}
