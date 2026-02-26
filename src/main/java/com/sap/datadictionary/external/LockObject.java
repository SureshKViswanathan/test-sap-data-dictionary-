package com.sap.datadictionary.external;

import com.sap.datadictionary.conceptual.TableDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an SAP Data Dictionary Lock Object.
 * <p>
 * Lock Objects belong to the External Schema because they govern how
 * concurrent users interact with shared data. A Lock Object references
 * a primary table and optional secondary tables, and generates enqueue
 * and dequeue function modules for application-level locking.
 * </p>
 */
public class LockObject {

    /** SAP lock modes. */
    public enum LockMode {
        /** Shared lock (read) */
        SHARED,
        /** Exclusive lock (write) */
        EXCLUSIVE,
        /** Exclusive but not cumulative */
        EXCLUSIVE_NON_CUMULATIVE
    }

    private final String name;
    private final TableDefinition primaryTable;
    private final List<TableDefinition> secondaryTables;
    private LockMode lockMode;
    private String description;

    public LockObject(String name, TableDefinition primaryTable) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Lock object name must not be blank");
        }
        if (primaryTable == null) {
            throw new IllegalArgumentException("Primary table must not be null");
        }
        this.name = name;
        this.primaryTable = primaryTable;
        this.secondaryTables = new ArrayList<>();
        this.lockMode = LockMode.EXCLUSIVE;
    }

    public void addSecondaryTable(TableDefinition table) {
        if (table == null) {
            throw new IllegalArgumentException("Secondary table must not be null");
        }
        secondaryTables.add(table);
    }

    public String getName() {
        return name;
    }

    public TableDefinition getPrimaryTable() {
        return primaryTable;
    }

    public List<TableDefinition> getSecondaryTables() {
        return Collections.unmodifiableList(secondaryTables);
    }

    public LockMode getLockMode() {
        return lockMode;
    }

    public void setLockMode(LockMode lockMode) {
        this.lockMode = lockMode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "LockObject{name='" + name + "', primaryTable="
                + primaryTable.getTableName() + ", mode=" + lockMode + '}';
    }
}
