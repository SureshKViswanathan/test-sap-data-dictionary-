package com.sap.datadictionary.registry;

import com.sap.datadictionary.conceptual.Structure;
import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.external.LockObject;
import com.sap.datadictionary.external.SearchHelp;
import com.sap.datadictionary.external.ViewDefinition;
import com.sap.datadictionary.internal.DataElement;
import com.sap.datadictionary.internal.Domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central metadata registry for the SAP Data Dictionary.
 * <p>
 * The {@code DataDictionary} acts as the catalog that ties together all
 * three layers of the ANSI/SPARC architecture:
 * </p>
 * <ul>
 *   <li><b>Internal Schema</b> – {@link Domain}s and {@link DataElement}s</li>
 *   <li><b>Conceptual Schema</b> – {@link TableDefinition}s and {@link Structure}s</li>
 *   <li><b>External Schema</b> – {@link ViewDefinition}s, {@link SearchHelp}s,
 *       and {@link LockObject}s</li>
 * </ul>
 */
public class DataDictionary {

    // Internal Schema
    private final Map<String, Domain> domains = new LinkedHashMap<>();
    private final Map<String, DataElement> dataElements = new LinkedHashMap<>();

    // Conceptual Schema
    private final Map<String, TableDefinition> tables = new LinkedHashMap<>();
    private final Map<String, Structure> structures = new LinkedHashMap<>();

    // External Schema
    private final Map<String, ViewDefinition> views = new LinkedHashMap<>();
    private final Map<String, SearchHelp> searchHelps = new LinkedHashMap<>();
    private final Map<String, LockObject> lockObjects = new LinkedHashMap<>();

    // ---- Internal Schema operations ----

    public void registerDomain(Domain domain) {
        requireNonNull(domain, "Domain");
        requireUnique(domains, domain.getName(), "Domain");
        domains.put(domain.getName(), domain);
    }

    public Domain getDomain(String name) {
        return domains.get(name);
    }

    public Map<String, Domain> getDomains() {
        return Collections.unmodifiableMap(domains);
    }

    public void registerDataElement(DataElement element) {
        requireNonNull(element, "Data element");
        requireUnique(dataElements, element.getName(), "Data element");
        dataElements.put(element.getName(), element);
    }

    public DataElement getDataElement(String name) {
        return dataElements.get(name);
    }

    public Map<String, DataElement> getDataElements() {
        return Collections.unmodifiableMap(dataElements);
    }

    // ---- Conceptual Schema operations ----

    public void registerTable(TableDefinition table) {
        requireNonNull(table, "Table");
        requireUnique(tables, table.getTableName(), "Table");
        tables.put(table.getTableName(), table);
    }

    public TableDefinition getTable(String name) {
        return tables.get(name);
    }

    public Map<String, TableDefinition> getTables() {
        return Collections.unmodifiableMap(tables);
    }

    public void registerStructure(Structure structure) {
        requireNonNull(structure, "Structure");
        requireUnique(structures, structure.getStructureName(), "Structure");
        structures.put(structure.getStructureName(), structure);
    }

    public Structure getStructure(String name) {
        return structures.get(name);
    }

    public Map<String, Structure> getStructures() {
        return Collections.unmodifiableMap(structures);
    }

    // ---- External Schema operations ----

    public void registerView(ViewDefinition view) {
        requireNonNull(view, "View");
        requireUnique(views, view.getViewName(), "View");
        views.put(view.getViewName(), view);
    }

    public ViewDefinition getView(String name) {
        return views.get(name);
    }

    public Map<String, ViewDefinition> getViews() {
        return Collections.unmodifiableMap(views);
    }

    public void registerSearchHelp(SearchHelp searchHelp) {
        requireNonNull(searchHelp, "Search help");
        requireUnique(searchHelps, searchHelp.getName(), "Search help");
        searchHelps.put(searchHelp.getName(), searchHelp);
    }

    public SearchHelp getSearchHelp(String name) {
        return searchHelps.get(name);
    }

    public Map<String, SearchHelp> getSearchHelps() {
        return Collections.unmodifiableMap(searchHelps);
    }

    public void registerLockObject(LockObject lockObject) {
        requireNonNull(lockObject, "Lock object");
        requireUnique(lockObjects, lockObject.getName(), "Lock object");
        lockObjects.put(lockObject.getName(), lockObject);
    }

    public LockObject getLockObject(String name) {
        return lockObjects.get(name);
    }

    public Map<String, LockObject> getLockObjects() {
        return Collections.unmodifiableMap(lockObjects);
    }

    // ---- helpers ----

    private static void requireNonNull(Object obj, String label) {
        if (obj == null) {
            throw new IllegalArgumentException(label + " must not be null");
        }
    }

    private static void requireUnique(Map<String, ?> map, String key, String label) {
        if (map.containsKey(key)) {
            throw new IllegalArgumentException(label + " already registered: " + key);
        }
    }
}
