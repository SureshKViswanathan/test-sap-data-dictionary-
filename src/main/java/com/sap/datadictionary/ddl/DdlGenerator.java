package com.sap.datadictionary.ddl;

import com.sap.datadictionary.conceptual.FieldDefinition;
import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.external.ViewDefinition;
import com.sap.datadictionary.internal.Domain;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates SQL DDL statements from Data Dictionary definitions.
 * <p>
 * Supports {@code CREATE TABLE} generation from {@link TableDefinition}
 * and {@code CREATE VIEW} generation from {@link ViewDefinition}, with
 * dialect-specific SQL type mappings for {@link SqlDialect#POSTGRESQL},
 * {@link SqlDialect#H2}, and {@link SqlDialect#HANA}.
 * </p>
 *
 * <h3>Type Mapping per Dialect</h3>
 * <table border="1">
 *   <tr><th>DDIC Type</th><th>PostgreSQL</th><th>H2</th><th>SAP HANA</th></tr>
 *   <tr><td>CHAR(n)</td><td>CHAR(n)</td><td>CHAR(n)</td><td>NCHAR(n)</td></tr>
 *   <tr><td>STRING(n)</td><td>VARCHAR(n)</td><td>VARCHAR(n)</td><td>NVARCHAR(n)</td></tr>
 *   <tr><td>NUMC(n)</td><td>CHAR(n)</td><td>CHAR(n)</td><td>NCHAR(n)</td></tr>
 *   <tr><td>INTEGER</td><td>INTEGER</td><td>INTEGER</td><td>INTEGER</td></tr>
 *   <tr><td>DECIMAL(p,s)</td><td>DECIMAL(p,s)</td><td>DECIMAL(p,s)</td><td>DECIMAL(p,s)</td></tr>
 *   <tr><td>DATE</td><td>DATE</td><td>DATE</td><td>DATE</td></tr>
 *   <tr><td>TIME</td><td>TIME</td><td>TIME</td><td>TIME</td></tr>
 *   <tr><td>TIMESTAMP</td><td>TIMESTAMP</td><td>TIMESTAMP</td><td>TIMESTAMP</td></tr>
 *   <tr><td>RAW(n)</td><td>BYTEA</td><td>BINARY(n)</td><td>VARBINARY(n)</td></tr>
 * </table>
 */
public class DdlGenerator {

    /**
     * Generate a {@code CREATE TABLE} statement from a {@link TableDefinition}.
     * <p>
     * Columns that are not nullable get a {@code NOT NULL} constraint.
     * Key fields are collected into a {@code PRIMARY KEY} clause at the end.
     * </p>
     *
     * @param table   the table definition to generate DDL for; must not be null
     *                and must have at least one field
     * @param dialect the target SQL dialect; must not be null
     * @return the SQL {@code CREATE TABLE} statement (without trailing semicolon)
     * @throws IllegalArgumentException if {@code table} or {@code dialect} is null,
     *                                  or if the table has no fields
     */
    public String generateCreateTable(TableDefinition table, SqlDialect dialect) {
        if (table == null) {
            throw new IllegalArgumentException("Table must not be null");
        }
        if (dialect == null) {
            throw new IllegalArgumentException("Dialect must not be null");
        }
        List<FieldDefinition> fields = table.getFields();
        if (fields.isEmpty()) {
            throw new IllegalArgumentException(
                    "Table '" + table.getTableName() + "' has no fields defined");
        }

        List<FieldDefinition> keyFields = table.getKeyFields();
        boolean hasPrimaryKey = !keyFields.isEmpty();

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(table.getTableName()).append(" (\n");

        for (int i = 0; i < fields.size(); i++) {
            FieldDefinition field = fields.get(i);
            Domain domain = field.getDataElement().getDomain();
            sb.append("    ").append(field.getFieldName())
              .append(" ").append(toSqlType(domain, dialect));
            if (!field.isNullable()) {
                sb.append(" NOT NULL");
            }
            if (i < fields.size() - 1 || hasPrimaryKey) {
                sb.append(",");
            }
            sb.append("\n");
        }

        if (hasPrimaryKey) {
            String keyList = keyFields.stream()
                    .map(FieldDefinition::getFieldName)
                    .collect(Collectors.joining(", "));
            sb.append("    PRIMARY KEY (").append(keyList).append(")\n");
        }

        sb.append(")");
        return sb.toString();
    }

    /**
     * Generate a {@code CREATE VIEW} statement from a {@link ViewDefinition}.
     * <p>
     * If the view has no selected fields, {@code SELECT *} is generated.
     * When multiple base tables are present they appear as a comma-separated
     * list in the {@code FROM} clause.
     * </p>
     *
     * @param view    the view definition to generate DDL for; must not be null
     *                and must have at least one base table
     * @param dialect the target SQL dialect; must not be null
     * @return the SQL {@code CREATE VIEW} statement (without trailing semicolon)
     * @throws IllegalArgumentException if {@code view} or {@code dialect} is null,
     *                                  or if the view has no base tables
     */
    public String generateCreateView(ViewDefinition view, SqlDialect dialect) {
        if (view == null) {
            throw new IllegalArgumentException("View must not be null");
        }
        if (dialect == null) {
            throw new IllegalArgumentException("Dialect must not be null");
        }
        List<TableDefinition> baseTables = view.getBaseTables();
        if (baseTables.isEmpty()) {
            throw new IllegalArgumentException(
                    "View '" + view.getViewName() + "' has no base tables defined");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE VIEW ").append(view.getViewName()).append(" AS\n");
        sb.append("SELECT ");

        List<String> selectedFields = view.getSelectedFields();
        if (selectedFields.isEmpty()) {
            sb.append("*");
        } else {
            sb.append(String.join(", ", selectedFields));
        }

        sb.append("\nFROM ");
        String tableList = baseTables.stream()
                .map(TableDefinition::getTableName)
                .collect(Collectors.joining(", "));
        sb.append(tableList);

        return sb.toString();
    }

    /**
     * Map a {@link Domain}'s data type and length to a dialect-specific SQL type string.
     *
     * @param domain  the domain providing data type, length, and decimals
     * @param dialect the target SQL dialect
     * @return the SQL type string (e.g. {@code "CHAR(10)"}, {@code "DECIMAL(15, 2)"})
     */
    String toSqlType(Domain domain, SqlDialect dialect) {
        int length = domain.getLength();
        int decimals = domain.getDecimals();

        return switch (domain.getDataType()) {
            case CHAR -> dialect == SqlDialect.HANA
                    ? "NCHAR(" + length + ")"
                    : "CHAR(" + length + ")";
            case STRING -> dialect == SqlDialect.HANA
                    ? "NVARCHAR(" + length + ")"
                    : "VARCHAR(" + length + ")";
            case NUMC -> dialect == SqlDialect.HANA
                    ? "NCHAR(" + length + ")"
                    : "CHAR(" + length + ")";
            case INTEGER -> "INTEGER";
            case DECIMAL -> "DECIMAL(" + length + ", " + decimals + ")";
            case DATE -> "DATE";
            case TIME -> "TIME";
            case TIMESTAMP -> "TIMESTAMP";
            case RAW -> switch (dialect) {
                case POSTGRESQL -> "BYTEA";
                case H2 -> "BINARY(" + length + ")";
                case HANA -> "VARBINARY(" + length + ")";
            };
        };
    }
}
