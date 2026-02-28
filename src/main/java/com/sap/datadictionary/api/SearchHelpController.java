package com.sap.datadictionary.api;

import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.external.SearchHelp;
import com.sap.datadictionary.registry.DataDictionary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Search Help CRUD operations.
 */
@RestController
@RequestMapping("/api/search-helps")
public class SearchHelpController {

    private final DataDictionary dictionary;

    public SearchHelpController(DataDictionary dictionary) {
        this.dictionary = dictionary;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return dictionary.getSearchHelps().values().stream()
                .map(SearchHelpController::toMap)
                .toList();
    }

    @GetMapping("/{name}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String name) {
        SearchHelp help = dictionary.getSearchHelp(name);
        if (help == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toMap(help));
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            SearchHelp help = new SearchHelp(name);

            if (body.containsKey("description")) {
                help.setDescription((String) body.get("description"));
            }

            if (body.containsKey("selectionMethodName")) {
                String tableName = (String) body.get("selectionMethodName");
                TableDefinition table = dictionary.getTable(tableName);
                if (table == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Table not found: " + tableName));
                }
                help.setSelectionMethod(table);
            }

            if (body.containsKey("displayFields")) {
                @SuppressWarnings("unchecked")
                List<String> fields = (List<String>) body.get("displayFields");
                for (String field : fields) {
                    help.addDisplayField(field);
                }
            }

            if (body.containsKey("exportFields")) {
                @SuppressWarnings("unchecked")
                List<String> fields = (List<String>) body.get("exportFields");
                for (String field : fields) {
                    help.addExportField(field);
                }
            }

            dictionary.registerSearchHelp(help);
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(help));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    static Map<String, Object> toMap(SearchHelp help) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", help.getName());
        map.put("description", help.getDescription());
        map.put("selectionMethodName",
                help.getSelectionMethod() != null
                        ? help.getSelectionMethod().getTableName() : null);
        map.put("displayFields", help.getDisplayFields());
        map.put("exportFields", help.getExportFields());
        return map;
    }
}
