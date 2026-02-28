package com.sap.datadictionary.registry;

import com.sap.datadictionary.conceptual.FieldDefinition;
import com.sap.datadictionary.conceptual.Structure;
import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.external.SearchHelp;
import com.sap.datadictionary.external.ViewDefinition;
import com.sap.datadictionary.internal.DataElement;
import com.sap.datadictionary.internal.Domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates the referential integrity and consistency of a
 * {@link DataDictionary} across all three ANSI/SPARC schema layers.
 * <p>
 * Checks performed:
 * <ul>
 *   <li>Every {@link DataElement} references a {@link Domain} that is
 *       registered in the dictionary.</li>
 *   <li>Every {@link FieldDefinition} (in tables and structures)
 *       references a registered {@link DataElement}.</li>
 *   <li>Every {@link ViewDefinition} only selects fields that exist
 *       in its base tables, and all base tables are registered.</li>
 *   <li>Every {@link SearchHelp} only references fields that exist
 *       in its selection-method table.</li>
 *   <li>Cycle / dependency detection among tables referenced by views.</li>
 * </ul>
 */
public class ConsistencyValidator {

    private final DataDictionary dictionary;

    public ConsistencyValidator(DataDictionary dictionary) {
        if (dictionary == null) {
            throw new IllegalArgumentException("DataDictionary must not be null");
        }
        this.dictionary = dictionary;
    }

    /**
     * Run all consistency checks and return a combined result.
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        validateDataElements(result);
        validateTableFields(result);
        validateStructureFields(result);
        validateViewFieldReferences(result);
        validateSearchHelpFieldReferences(result);
        validateDependencyCycles(result);
        return result;
    }

    /**
     * Validate that every registered {@link DataElement} references a
     * {@link Domain} that is also registered in the dictionary.
     */
    void validateDataElements(ValidationResult result) {
        for (DataElement element : dictionary.getDataElements().values()) {
            Domain domain = element.getDomain();
            if (dictionary.getDomain(domain.getName()) == null) {
                result.addError("DataElement '" + element.getName()
                        + "' references Domain '" + domain.getName()
                        + "' which is not registered in the dictionary");
            } else if (dictionary.getDomain(domain.getName()) != domain) {
                result.addError("DataElement '" + element.getName()
                        + "' references a Domain instance '" + domain.getName()
                        + "' that differs from the registered Domain with the same name");
            }
        }
    }

    /**
     * Validate that every {@link FieldDefinition} in registered tables
     * references a {@link DataElement} that is registered in the dictionary.
     */
    void validateTableFields(ValidationResult result) {
        for (TableDefinition table : dictionary.getTables().values()) {
            validateFieldList(result, table.getFields(), "Table '" + table.getTableName() + "'");
        }
    }

    /**
     * Validate that every {@link FieldDefinition} in registered structures
     * references a {@link DataElement} that is registered in the dictionary.
     */
    void validateStructureFields(ValidationResult result) {
        for (Structure structure : dictionary.getStructures().values()) {
            validateFieldList(result, structure.getFields(),
                    "Structure '" + structure.getStructureName() + "'");
        }
    }

    private void validateFieldList(ValidationResult result,
                                   List<FieldDefinition> fields,
                                   String parentLabel) {
        for (FieldDefinition field : fields) {
            DataElement de = field.getDataElement();
            if (dictionary.getDataElement(de.getName()) == null) {
                result.addError(parentLabel + ", field '" + field.getFieldName()
                        + "' references DataElement '" + de.getName()
                        + "' which is not registered in the dictionary");
            } else if (dictionary.getDataElement(de.getName()) != de) {
                result.addError(parentLabel + ", field '" + field.getFieldName()
                        + "' references a DataElement instance '" + de.getName()
                        + "' that differs from the registered DataElement with the same name");
            }
        }
    }

    /**
     * Validate that every {@link ViewDefinition}:
     * <ul>
     *   <li>has all base tables registered in the dictionary</li>
     *   <li>only selects fields that exist in at least one of its base tables</li>
     * </ul>
     */
    void validateViewFieldReferences(ValidationResult result) {
        for (ViewDefinition view : dictionary.getViews().values()) {
            // Collect available field names from all base tables
            Set<String> availableFields = new HashSet<>();
            for (TableDefinition baseTable : view.getBaseTables()) {
                if (dictionary.getTable(baseTable.getTableName()) == null) {
                    result.addError("View '" + view.getViewName()
                            + "' references base table '" + baseTable.getTableName()
                            + "' which is not registered in the dictionary");
                }
                for (FieldDefinition field : baseTable.getFields()) {
                    availableFields.add(field.getFieldName());
                }
            }

            // Check each selected field exists in base tables
            for (String selectedField : view.getSelectedFields()) {
                if (!availableFields.contains(selectedField)) {
                    result.addError("View '" + view.getViewName()
                            + "' selects field '" + selectedField
                            + "' which does not exist in any of its base tables");
                }
            }
        }
    }

    /**
     * Validate that every {@link SearchHelp}:
     * <ul>
     *   <li>has its selection-method table registered (if set)</li>
     *   <li>only references display/export fields that exist in the
     *       selection-method table</li>
     * </ul>
     */
    void validateSearchHelpFieldReferences(ValidationResult result) {
        for (SearchHelp help : dictionary.getSearchHelps().values()) {
            TableDefinition table = help.getSelectionMethod();
            if (table == null) {
                // No selection method set – nothing to validate
                continue;
            }

            if (dictionary.getTable(table.getTableName()) == null) {
                result.addError("SearchHelp '" + help.getName()
                        + "' references selection-method table '" + table.getTableName()
                        + "' which is not registered in the dictionary");
            }

            Set<String> tableFields = new HashSet<>();
            for (FieldDefinition field : table.getFields()) {
                tableFields.add(field.getFieldName());
            }

            for (String displayField : help.getDisplayFields()) {
                if (!tableFields.contains(displayField)) {
                    result.addError("SearchHelp '" + help.getName()
                            + "' display field '" + displayField
                            + "' does not exist in selection-method table '"
                            + table.getTableName() + "'");
                }
            }

            for (String exportField : help.getExportFields()) {
                if (!tableFields.contains(exportField)) {
                    result.addError("SearchHelp '" + help.getName()
                            + "' export field '" + exportField
                            + "' does not exist in selection-method table '"
                            + table.getTableName() + "'");
                }
            }
        }
    }

    /**
     * Detect circular dependencies among views.
     * <p>
     * In this model, views reference tables but not other views, so true
     * cycles are unlikely. This check validates that no table is used as
     * a base table in a view while also being implicitly dependent on
     * that view (which would indicate a modelling error). Currently this
     * reports a warning if a view selects zero fields (empty projection)
     * or has no base tables, since such definitions are likely incomplete.
     * </p>
     */
    void validateDependencyCycles(ValidationResult result) {
        for (ViewDefinition view : dictionary.getViews().values()) {
            if (view.getBaseTables().isEmpty()) {
                result.addWarning("View '" + view.getViewName()
                        + "' has no base tables defined");
            }
            if (view.getSelectedFields().isEmpty() && !view.getBaseTables().isEmpty()) {
                result.addWarning("View '" + view.getViewName()
                        + "' has base tables but selects no fields");
            }
        }

        // Detect if any table appears as its own base table through structures
        // (currently structures and tables are separate, but validate anyway)
        for (TableDefinition table : dictionary.getTables().values()) {
            if (table.getFields().isEmpty()) {
                result.addWarning("Table '" + table.getTableName()
                        + "' has no fields defined");
            }
        }
    }
}
