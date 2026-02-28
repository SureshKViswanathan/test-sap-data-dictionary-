package com.sap.datadictionary.api;

import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.external.LockObject;
import com.sap.datadictionary.registry.DataDictionary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Lock Object CRUD operations.
 */
@RestController
@RequestMapping("/api/lock-objects")
public class LockObjectController {

    private final DataDictionary dictionary;

    public LockObjectController(DataDictionary dictionary) {
        this.dictionary = dictionary;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return dictionary.getLockObjects().values().stream()
                .map(LockObjectController::toMap)
                .toList();
    }

    @GetMapping("/{name}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String name) {
        LockObject lock = dictionary.getLockObject(name);
        if (lock == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toMap(lock));
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            String primaryTableName = (String) body.get("primaryTableName");

            TableDefinition primaryTable = dictionary.getTable(primaryTableName);
            if (primaryTable == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Primary table not found: " + primaryTableName));
            }

            LockObject lock = new LockObject(name, primaryTable);

            if (body.containsKey("description")) {
                lock.setDescription((String) body.get("description"));
            }
            if (body.containsKey("lockMode")) {
                lock.setLockMode(LockObject.LockMode.valueOf((String) body.get("lockMode")));
            }

            if (body.containsKey("secondaryTableNames")) {
                @SuppressWarnings("unchecked")
                List<String> tableNames = (List<String>) body.get("secondaryTableNames");
                for (String tableName : tableNames) {
                    TableDefinition table = dictionary.getTable(tableName);
                    if (table == null) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Secondary table not found: " + tableName));
                    }
                    lock.addSecondaryTable(table);
                }
            }

            dictionary.registerLockObject(lock);
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(lock));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    static Map<String, Object> toMap(LockObject lock) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", lock.getName());
        map.put("description", lock.getDescription());
        map.put("primaryTableName", lock.getPrimaryTable().getTableName());
        map.put("secondaryTableNames", lock.getSecondaryTables().stream()
                .map(TableDefinition::getTableName)
                .toList());
        map.put("lockMode", lock.getLockMode().name());
        return map;
    }
}
