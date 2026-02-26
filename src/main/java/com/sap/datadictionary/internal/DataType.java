package com.sap.datadictionary.internal;

/**
 * Supported data types within the Internal Schema, mirroring SAP ABAP
 * built-in types used in the Data Dictionary.
 */
public enum DataType {

    /** Character string (CHAR) */
    CHAR,
    /** Variable-length character string (VARCHAR / STRING) */
    STRING,
    /** Numeric text (NUMC) */
    NUMC,
    /** Integer (INT4) */
    INTEGER,
    /** Packed decimal (DEC / CURR / QUAN) */
    DECIMAL,
    /** Date (DATS, format YYYYMMDD) */
    DATE,
    /** Time (TIMS, format HHMMSS) */
    TIME,
    /** Timestamp */
    TIMESTAMP,
    /** Raw byte sequence (RAW) */
    RAW
}
