package com.sap.datadictionary.api;

import com.sap.datadictionary.internal.DataElement;
import com.sap.datadictionary.internal.Domain;
import com.sap.datadictionary.registry.DataDictionary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Data Element CRUD operations.
 */
@RestController
@RequestMapping("/api/data-elements")
public class DataElementController {

    private final DataDictionary dictionary;

    public DataElementController(DataDictionary dictionary) {
        this.dictionary = dictionary;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return dictionary.getDataElements().values().stream()
                .map(DataElementController::toMap)
                .toList();
    }

    @GetMapping("/{name}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String name) {
        DataElement element = dictionary.getDataElement(name);
        if (element == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toMap(element));
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            String domainName = (String) body.get("domainName");

            Domain domain = dictionary.getDomain(domainName);
            if (domain == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Domain not found: " + domainName));
            }

            DataElement element = new DataElement(name, domain);

            if (body.containsKey("shortLabel")) {
                element.setShortLabel((String) body.get("shortLabel"));
            }
            if (body.containsKey("mediumLabel")) {
                element.setMediumLabel((String) body.get("mediumLabel"));
            }
            if (body.containsKey("longLabel")) {
                element.setLongLabel((String) body.get("longLabel"));
            }
            if (body.containsKey("documentation")) {
                element.setDocumentation((String) body.get("documentation"));
            }

            dictionary.registerDataElement(element);
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(element));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    static Map<String, Object> toMap(DataElement element) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", element.getName());
        map.put("domainName", element.getDomain().getName());
        map.put("shortLabel", element.getShortLabel());
        map.put("mediumLabel", element.getMediumLabel());
        map.put("longLabel", element.getLongLabel());
        map.put("documentation", element.getDocumentation());
        return map;
    }
}
