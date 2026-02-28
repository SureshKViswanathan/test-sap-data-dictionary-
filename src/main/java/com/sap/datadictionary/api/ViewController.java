package com.sap.datadictionary.api;

import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.external.ViewDefinition;
import com.sap.datadictionary.registry.DataDictionary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for View CRUD operations.
 */
@RestController
@RequestMapping("/api/views")
public class ViewController {

    private final DataDictionary dictionary;

    public ViewController(DataDictionary dictionary) {
        this.dictionary = dictionary;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return dictionary.getViews().values().stream()
                .map(ViewController::toMap)
                .toList();
    }

    @GetMapping("/{name}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String name) {
        ViewDefinition view = dictionary.getView(name);
        if (view == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toMap(view));
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Map<String, Object> body) {
        try {
            String viewName = (String) body.get("viewName");
            ViewDefinition.ViewType viewType =
                    ViewDefinition.ViewType.valueOf((String) body.get("viewType"));

            ViewDefinition view = new ViewDefinition(viewName, viewType);

            if (body.containsKey("description")) {
                view.setDescription((String) body.get("description"));
            }

            if (body.containsKey("baseTableNames")) {
                @SuppressWarnings("unchecked")
                List<String> tableNames = (List<String>) body.get("baseTableNames");
                for (String tableName : tableNames) {
                    TableDefinition table = dictionary.getTable(tableName);
                    if (table == null) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Table not found: " + tableName));
                    }
                    view.addBaseTable(table);
                }
            }

            if (body.containsKey("selectedFields")) {
                @SuppressWarnings("unchecked")
                List<String> fields = (List<String>) body.get("selectedFields");
                for (String field : fields) {
                    view.addSelectedField(field);
                }
            }

            dictionary.registerView(view);
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(view));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    static Map<String, Object> toMap(ViewDefinition view) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("viewName", view.getViewName());
        map.put("viewType", view.getViewType().name());
        map.put("description", view.getDescription());
        map.put("baseTableNames", view.getBaseTables().stream()
                .map(TableDefinition::getTableName)
                .toList());
        map.put("selectedFields", view.getSelectedFields());
        return map;
    }
}
