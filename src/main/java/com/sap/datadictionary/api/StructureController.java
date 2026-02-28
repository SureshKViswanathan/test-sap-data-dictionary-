package com.sap.datadictionary.api;

import com.sap.datadictionary.conceptual.FieldDefinition;
import com.sap.datadictionary.conceptual.Structure;
import com.sap.datadictionary.internal.DataElement;
import com.sap.datadictionary.registry.DataDictionary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Structure CRUD operations.
 */
@RestController
@RequestMapping("/api/structures")
public class StructureController {

    private final DataDictionary dictionary;

    public StructureController(DataDictionary dictionary) {
        this.dictionary = dictionary;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return dictionary.getStructures().values().stream()
                .map(StructureController::toMap)
                .toList();
    }

    @GetMapping("/{name}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String name) {
        Structure structure = dictionary.getStructure(name);
        if (structure == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toMap(structure));
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Map<String, Object> body) {
        try {
            String structureName = (String) body.get("structureName");
            Structure structure = new Structure(structureName);

            if (body.containsKey("description")) {
                structure.setDescription((String) body.get("description"));
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
                    structure.addField(new FieldDefinition(fieldName, de, keyField, nullable));
                }
            }

            dictionary.registerStructure(structure);
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(structure));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    static Map<String, Object> toMap(Structure structure) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("structureName", structure.getStructureName());
        map.put("description", structure.getDescription());
        map.put("fields", structure.getFields().stream()
                .map(TableController::fieldToMap)
                .toList());
        return map;
    }
}
