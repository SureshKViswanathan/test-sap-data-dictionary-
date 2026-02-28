package com.sap.datadictionary.registry;

import com.sap.datadictionary.conceptual.FieldDefinition;
import com.sap.datadictionary.conceptual.Structure;
import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.external.LockObject;
import com.sap.datadictionary.external.SearchHelp;
import com.sap.datadictionary.external.ViewDefinition;
import com.sap.datadictionary.internal.DataElement;
import com.sap.datadictionary.internal.Domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Performs where-used analysis across the Data Dictionary.
 * <p>
 * Given a DDIC object, finds all other objects that reference it.
 * This enables impact analysis when changing or deleting an object.
 * </p>
 */
public class WhereUsedAnalyzer {

    private final DataDictionary dictionary;

    public WhereUsedAnalyzer(DataDictionary dictionary) {
        if (dictionary == null) {
            throw new IllegalArgumentException("DataDictionary must not be null");
        }
        this.dictionary = dictionary;
    }

    /**
     * Find all Data Elements that reference the given Domain.
     */
    public List<String> findDataElementsUsingDomain(String domainName) {
        List<String> result = new ArrayList<>();
        for (DataElement element : dictionary.getDataElements().values()) {
            if (element.getDomain().getName().equals(domainName)) {
                result.add(element.getName());
            }
        }
        return result;
    }

    /**
     * Find all Tables and Structures that have fields using the given Data Element.
     */
    public Map<String, List<String>> findTablesUsingDataElement(String dataElementName) {
        Map<String, List<String>> result = new LinkedHashMap<>();

        List<String> tables = new ArrayList<>();
        for (TableDefinition table : dictionary.getTables().values()) {
            for (FieldDefinition field : table.getFields()) {
                if (field.getDataElement().getName().equals(dataElementName)) {
                    tables.add(table.getTableName());
                    break;
                }
            }
        }
        if (!tables.isEmpty()) {
            result.put("tables", tables);
        }

        List<String> structures = new ArrayList<>();
        for (Structure structure : dictionary.getStructures().values()) {
            for (FieldDefinition field : structure.getFields()) {
                if (field.getDataElement().getName().equals(dataElementName)) {
                    structures.add(structure.getStructureName());
                    break;
                }
            }
        }
        if (!structures.isEmpty()) {
            result.put("structures", structures);
        }

        return result;
    }

    /**
     * Find all objects that reference the given Table: Views, Search Helps, Lock Objects.
     */
    public Map<String, List<String>> findUsagesOfTable(String tableName) {
        Map<String, List<String>> result = new LinkedHashMap<>();

        List<String> views = new ArrayList<>();
        for (ViewDefinition view : dictionary.getViews().values()) {
            for (TableDefinition baseTable : view.getBaseTables()) {
                if (baseTable.getTableName().equals(tableName)) {
                    views.add(view.getViewName());
                    break;
                }
            }
        }
        if (!views.isEmpty()) {
            result.put("views", views);
        }

        List<String> searchHelps = new ArrayList<>();
        for (SearchHelp help : dictionary.getSearchHelps().values()) {
            if (help.getSelectionMethod() != null
                    && help.getSelectionMethod().getTableName().equals(tableName)) {
                searchHelps.add(help.getName());
            }
        }
        if (!searchHelps.isEmpty()) {
            result.put("searchHelps", searchHelps);
        }

        List<String> lockObjects = new ArrayList<>();
        for (LockObject lock : dictionary.getLockObjects().values()) {
            if (lock.getPrimaryTable().getTableName().equals(tableName)) {
                lockObjects.add(lock.getName());
            } else {
                for (TableDefinition sec : lock.getSecondaryTables()) {
                    if (sec.getTableName().equals(tableName)) {
                        lockObjects.add(lock.getName());
                        break;
                    }
                }
            }
        }
        if (!lockObjects.isEmpty()) {
            result.put("lockObjects", lockObjects);
        }

        return result;
    }

    /**
     * Perform a full where-used analysis for a Domain across all layers.
     * Returns a map with keys: dataElements, tables, structures, views, searchHelps, lockObjects.
     */
    public Map<String, List<String>> findAllUsagesOfDomain(String domainName) {
        Map<String, List<String>> result = new LinkedHashMap<>();

        List<String> dataElements = findDataElementsUsingDomain(domainName);
        if (!dataElements.isEmpty()) {
            result.put("dataElements", dataElements);
        }

        // Find tables/structures that use those data elements
        List<String> tables = new ArrayList<>();
        List<String> structures = new ArrayList<>();
        for (String deName : dataElements) {
            Map<String, List<String>> deUsages = findTablesUsingDataElement(deName);
            if (deUsages.containsKey("tables")) {
                for (String t : deUsages.get("tables")) {
                    if (!tables.contains(t)) {
                        tables.add(t);
                    }
                }
            }
            if (deUsages.containsKey("structures")) {
                for (String s : deUsages.get("structures")) {
                    if (!structures.contains(s)) {
                        structures.add(s);
                    }
                }
            }
        }
        if (!tables.isEmpty()) {
            result.put("tables", tables);
        }
        if (!structures.isEmpty()) {
            result.put("structures", structures);
        }

        return result;
    }
}
