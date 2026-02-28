package com.sap.datadictionary.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sap.datadictionary.conceptual.FieldDefinition;
import com.sap.datadictionary.conceptual.Structure;
import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.external.LockObject;
import com.sap.datadictionary.external.SearchHelp;
import com.sap.datadictionary.external.ViewDefinition;
import com.sap.datadictionary.internal.DataElement;
import com.sap.datadictionary.internal.DataType;
import com.sap.datadictionary.internal.Domain;
import com.sap.datadictionary.internal.ValueRange;
import com.sap.datadictionary.persistence.DictionarySnapshot.*;
import com.sap.datadictionary.registry.DataDictionary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts a {@link DataDictionary} to and from JSON via an intermediate
 * {@link DictionarySnapshot} representation.
 */
public class DictionarySerializer {

    private final ObjectMapper mapper;

    public DictionarySerializer() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // ---- public API ----

    /** Serialize a {@link DataDictionary} to a JSON string. */
    public String toJson(DataDictionary dictionary) {
        try {
            return mapper.writeValueAsString(toSnapshot(dictionary));
        } catch (JsonProcessingException e) {
            throw new DictionarySerializationException("Failed to serialize dictionary to JSON", e);
        }
    }

    /** Deserialize a JSON string back into a fully-wired {@link DataDictionary}. */
    public DataDictionary fromJson(String json) {
        try {
            DictionarySnapshot snapshot = mapper.readValue(json, DictionarySnapshot.class);
            return fromSnapshot(snapshot);
        } catch (JsonProcessingException e) {
            throw new DictionarySerializationException("Failed to deserialize dictionary from JSON", e);
        }
    }

    // ---- snapshot conversion ----

    /** Convert a live {@link DataDictionary} into a serializable snapshot. */
    public DictionarySnapshot toSnapshot(DataDictionary dictionary) {
        Map<String, DomainDto> domains = new LinkedHashMap<>();
        for (Domain d : dictionary.getDomains().values()) {
            List<String> fixedValues = List.of();
            if (d.getValueRange() != null) {
                fixedValues = new ArrayList<>(d.getValueRange().getFixedValues());
            }
            domains.put(d.getName(), new DomainDto(
                    d.getName(), d.getDataType().name(), d.getLength(),
                    d.getDecimals(), d.getDescription(), fixedValues));
        }

        Map<String, DataElementDto> dataElements = new LinkedHashMap<>();
        for (DataElement de : dictionary.getDataElements().values()) {
            dataElements.put(de.getName(), new DataElementDto(
                    de.getName(), de.getDomain().getName(),
                    de.getShortLabel(), de.getMediumLabel(),
                    de.getLongLabel(), de.getDocumentation()));
        }

        Map<String, TableDto> tables = new LinkedHashMap<>();
        for (TableDefinition t : dictionary.getTables().values()) {
            tables.put(t.getTableName(), new TableDto(
                    t.getTableName(), t.getDescription(),
                    t.getDeliveryClass().name(), t.isBuffered(),
                    toFieldDtos(t.getFields())));
        }

        Map<String, StructureDto> structures = new LinkedHashMap<>();
        for (Structure s : dictionary.getStructures().values()) {
            structures.put(s.getStructureName(), new StructureDto(
                    s.getStructureName(), s.getDescription(),
                    toFieldDtos(s.getFields())));
        }

        Map<String, ViewDto> views = new LinkedHashMap<>();
        for (ViewDefinition v : dictionary.getViews().values()) {
            List<String> baseTableNames = v.getBaseTables().stream()
                    .map(TableDefinition::getTableName).toList();
            views.put(v.getViewName(), new ViewDto(
                    v.getViewName(), v.getViewType().name(),
                    baseTableNames, new ArrayList<>(v.getSelectedFields()),
                    v.getDescription()));
        }

        Map<String, SearchHelpDto> searchHelps = new LinkedHashMap<>();
        for (SearchHelp sh : dictionary.getSearchHelps().values()) {
            String selMethod = sh.getSelectionMethod() != null
                    ? sh.getSelectionMethod().getTableName() : null;
            searchHelps.put(sh.getName(), new SearchHelpDto(
                    sh.getName(), selMethod,
                    new ArrayList<>(sh.getDisplayFields()),
                    new ArrayList<>(sh.getExportFields()),
                    sh.getDescription()));
        }

        Map<String, LockObjectDto> lockObjects = new LinkedHashMap<>();
        for (LockObject lo : dictionary.getLockObjects().values()) {
            List<String> secTables = lo.getSecondaryTables().stream()
                    .map(TableDefinition::getTableName).toList();
            lockObjects.put(lo.getName(), new LockObjectDto(
                    lo.getName(), lo.getPrimaryTable().getTableName(),
                    secTables, lo.getLockMode().name(), lo.getDescription()));
        }

        return new DictionarySnapshot(domains, dataElements, tables,
                structures, views, searchHelps, lockObjects);
    }

    /** Reconstruct a live {@link DataDictionary} from a snapshot. */
    public DataDictionary fromSnapshot(DictionarySnapshot snapshot) {
        DataDictionary dd = new DataDictionary();

        // 1. Domains (no external references)
        for (DomainDto dto : snapshot.domains().values()) {
            Domain domain = new Domain(dto.name(),
                    DataType.valueOf(dto.dataType()),
                    dto.length(), dto.decimals());
            domain.setDescription(dto.description());
            if (dto.fixedValues() != null && !dto.fixedValues().isEmpty()) {
                ValueRange vr = new ValueRange();
                dto.fixedValues().forEach(vr::addFixedValue);
                domain.setValueRange(vr);
            }
            dd.registerDomain(domain);
        }

        // 2. Data Elements (reference Domains by name)
        for (DataElementDto dto : snapshot.dataElements().values()) {
            Domain domain = dd.getDomain(dto.domainName());
            if (domain == null) {
                throw new DictionarySerializationException(
                        "Domain not found: " + dto.domainName());
            }
            DataElement element = new DataElement(dto.name(), domain);
            element.setShortLabel(dto.shortLabel());
            element.setMediumLabel(dto.mediumLabel());
            element.setLongLabel(dto.longLabel());
            element.setDocumentation(dto.documentation());
            dd.registerDataElement(element);
        }

        // 3. Tables (fields reference DataElements by name)
        for (TableDto dto : snapshot.tables().values()) {
            TableDefinition table = new TableDefinition(dto.tableName());
            table.setDescription(dto.description());
            table.setDeliveryClass(TableDefinition.DeliveryClass.valueOf(dto.deliveryClass()));
            table.setBuffered(dto.buffered());
            for (FieldDto f : dto.fields()) {
                DataElement de = dd.getDataElement(f.dataElementName());
                if (de == null) {
                    throw new DictionarySerializationException(
                            "DataElement not found: " + f.dataElementName());
                }
                table.addField(new FieldDefinition(f.fieldName(), de, f.keyField(), f.nullable()));
            }
            dd.registerTable(table);
        }

        // 4. Structures (fields reference DataElements by name)
        for (StructureDto dto : snapshot.structures().values()) {
            Structure structure = new Structure(dto.structureName());
            structure.setDescription(dto.description());
            for (FieldDto f : dto.fields()) {
                DataElement de = dd.getDataElement(f.dataElementName());
                if (de == null) {
                    throw new DictionarySerializationException(
                            "DataElement not found: " + f.dataElementName());
                }
                structure.addField(new FieldDefinition(f.fieldName(), de, f.keyField(), f.nullable()));
            }
            dd.registerStructure(structure);
        }

        // 5. Views (reference Tables by name)
        for (ViewDto dto : snapshot.views().values()) {
            ViewDefinition view = new ViewDefinition(dto.viewName(),
                    ViewDefinition.ViewType.valueOf(dto.viewType()));
            view.setDescription(dto.description());
            for (String tableName : dto.baseTableNames()) {
                TableDefinition table = dd.getTable(tableName);
                if (table == null) {
                    throw new DictionarySerializationException(
                            "Table not found: " + tableName);
                }
                view.addBaseTable(table);
            }
            dto.selectedFields().forEach(view::addSelectedField);
            dd.registerView(view);
        }

        // 6. Search Helps (reference Tables by name)
        for (SearchHelpDto dto : snapshot.searchHelps().values()) {
            SearchHelp help = new SearchHelp(dto.name());
            help.setDescription(dto.description());
            if (dto.selectionMethodName() != null) {
                TableDefinition table = dd.getTable(dto.selectionMethodName());
                if (table == null) {
                    throw new DictionarySerializationException(
                            "Table not found: " + dto.selectionMethodName());
                }
                help.setSelectionMethod(table);
            }
            dto.displayFields().forEach(help::addDisplayField);
            dto.exportFields().forEach(help::addExportField);
            dd.registerSearchHelp(help);
        }

        // 7. Lock Objects (reference Tables by name)
        for (LockObjectDto dto : snapshot.lockObjects().values()) {
            TableDefinition primary = dd.getTable(dto.primaryTableName());
            if (primary == null) {
                throw new DictionarySerializationException(
                        "Table not found: " + dto.primaryTableName());
            }
            LockObject lock = new LockObject(dto.name(), primary);
            lock.setLockMode(LockObject.LockMode.valueOf(dto.lockMode()));
            lock.setDescription(dto.description());
            for (String secName : dto.secondaryTableNames()) {
                TableDefinition sec = dd.getTable(secName);
                if (sec == null) {
                    throw new DictionarySerializationException(
                            "Table not found: " + secName);
                }
                lock.addSecondaryTable(sec);
            }
            dd.registerLockObject(lock);
        }

        return dd;
    }

    // ---- helpers ----

    private List<FieldDto> toFieldDtos(List<FieldDefinition> fields) {
        return fields.stream()
                .map(f -> new FieldDto(f.getFieldName(),
                        f.getDataElement().getName(),
                        f.isKeyField(), f.isNullable()))
                .toList();
    }
}
