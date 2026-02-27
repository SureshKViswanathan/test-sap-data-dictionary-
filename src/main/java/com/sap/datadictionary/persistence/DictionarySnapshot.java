package com.sap.datadictionary.persistence;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable, serialization-friendly snapshot of a {@code DataDictionary}.
 * <p>
 * Cross-object references (e.g. a DataElement referencing a Domain) are
 * stored as simple name strings so the snapshot can be serialized to JSON
 * without circular references.
 * </p>
 */
public record DictionarySnapshot(
        Map<String, DomainDto> domains,
        Map<String, DataElementDto> dataElements,
        Map<String, TableDto> tables,
        Map<String, StructureDto> structures,
        Map<String, ViewDto> views,
        Map<String, SearchHelpDto> searchHelps,
        Map<String, LockObjectDto> lockObjects
) {

    /** Create an empty snapshot. */
    public DictionarySnapshot() {
        this(new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>());
    }

    // ---- DTO records ----

    public record DomainDto(
            String name,
            String dataType,
            int length,
            int decimals,
            String description,
            List<String> fixedValues
    ) {}

    public record DataElementDto(
            String name,
            String domainName,
            String shortLabel,
            String mediumLabel,
            String longLabel,
            String documentation
    ) {}

    public record FieldDto(
            String fieldName,
            String dataElementName,
            boolean keyField,
            boolean nullable
    ) {}

    public record TableDto(
            String tableName,
            String description,
            String deliveryClass,
            boolean buffered,
            List<FieldDto> fields
    ) {}

    public record StructureDto(
            String structureName,
            String description,
            List<FieldDto> fields
    ) {}

    public record ViewDto(
            String viewName,
            String viewType,
            List<String> baseTableNames,
            List<String> selectedFields,
            String description
    ) {}

    public record SearchHelpDto(
            String name,
            String selectionMethodName,
            List<String> displayFields,
            List<String> exportFields,
            String description
    ) {}

    public record LockObjectDto(
            String name,
            String primaryTableName,
            List<String> secondaryTableNames,
            String lockMode,
            String description
    ) {}
}
