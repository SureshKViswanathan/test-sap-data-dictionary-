package com.sap.datadictionary.api;

import com.sap.datadictionary.conceptual.FieldDefinition;
import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.internal.DataElement;
import com.sap.datadictionary.registry.DataDictionary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Table CRUD operations.
 */
@RestController
@RequestMapping("/api/tables")
public class TableController {

    private final DataDictionary dictionary;

    public TableController(DataDictionary dictionary) {
        this.dictionary = dictionary;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return dictionary.getTables().values().stream()
                .map(TableController::toMap)
                .toList();
    }

    @GetMapping("/{name}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String name) {
        TableDefinition table = dictionary.getTable(name);
        if (table == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toMap(table));
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Map<String, Object> body) {
        try {
            String tableName = (String) body.get("tableName");
            TableDefinition table = new TableDefinition(tableName);

            if (body.containsKey("description")) {
                table.setDescription((String) body.get("description"));
            }
            if (body.containsKey("deliveryClass")) {
                table.setDeliveryClass(
                        TableDefinition.DeliveryClass.valueOf((String) body.get("deliveryClass")));
            }
            if (body.containsKey("buffered")) {
                table.setBuffered((Boolean) body.get("buffered"));
            }

            if (body.containsKey("fields")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> fields = (List<Map<String, Object>>) body.get("fields");
                for (Map<String, Object> fieldMap : fields) {
                    String fieldName = (String) fieldMap.get("fieldName");
                    String deName = (String) fieldMap.get("dataElementName");
                    boolean keyField = fieldMap.containsKey("keyField")
                            && (Boolean) fieldMap.get("keyField");
                    boolean nullable = fieldMap.containsKey("nullable")
                            && (Boolean) fieldMap.get("nullable");

                    DataElement de = dictionary.getDataElement(deName);
                    if (de == null) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Data element not found: " + deName));
                    }
                    table.addField(new FieldDefinition(fieldName, de, keyField, nullable));
                }
            }

            dictionary.registerTable(table);
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(table));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    static Map<String, Object> toMap(TableDefinition table) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("tableName", table.getTableName());
        map.put("description", table.getDescription());
        map.put("deliveryClass", table.getDeliveryClass().name());
        map.put("buffered", table.isBuffered());
        map.put("fields", table.getFields().stream()
                .map(TableController::fieldToMap)
                .toList());
        return map;
    }

    static Map<String, Object> fieldToMap(FieldDefinition field) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("fieldName", field.getFieldName());
        map.put("dataElementName", field.getDataElement().getName());
        map.put("keyField", field.isKeyField());
        map.put("nullable", field.isNullable());
        return map;
    }
}
