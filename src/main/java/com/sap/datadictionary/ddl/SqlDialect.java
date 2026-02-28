package com.sap.datadictionary.ddl;

/**
 * Supported SQL dialects for DDL generation.
 * <p>
 * Each dialect may use different SQL type names and syntax conventions.
 * Select the dialect that matches the target database system.
 * </p>
 */
public enum SqlDialect {

    /** PostgreSQL (open-source RDBMS) */
    POSTGRESQL,

    /** H2 (embedded/in-memory database, often used for testing) */
    H2,

    /** SAP HANA (SAP's in-memory column-store database) */
    HANA
}
