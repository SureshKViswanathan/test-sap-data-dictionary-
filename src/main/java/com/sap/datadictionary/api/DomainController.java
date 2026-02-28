package com.sap.datadictionary.api;

import com.sap.datadictionary.internal.DataType;
import com.sap.datadictionary.internal.Domain;
import com.sap.datadictionary.internal.ValueRange;
import com.sap.datadictionary.registry.DataDictionary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Domain CRUD operations.
 */
@RestController
@RequestMapping("/api/domains")
public class DomainController {

    private final DataDictionary dictionary;

    public DomainController(DataDictionary dictionary) {
        this.dictionary = dictionary;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return dictionary.getDomains().values().stream()
                .map(DomainController::toMap)
                .toList();
    }

    @GetMapping("/{name}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String name) {
        Domain domain = dictionary.getDomain(name);
        if (domain == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toMap(domain));
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            DataType dataType = DataType.valueOf((String) body.get("dataType"));
            int length = ((Number) body.get("length")).intValue();
            int decimals = body.containsKey("decimals")
                    ? ((Number) body.get("decimals")).intValue() : 0;

            Domain domain = new Domain(name, dataType, length, decimals);

            if (body.containsKey("description")) {
                domain.setDescription((String) body.get("description"));
            }

            if (body.containsKey("fixedValues")) {
                ValueRange range = new ValueRange();
                @SuppressWarnings("unchecked")
                List<String> values = (List<String>) body.get("fixedValues");
                for (String v : values) {
                    range.addFixedValue(v);
                }
                domain.setValueRange(range);
            }

            dictionary.registerDomain(domain);
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(domain));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    static Map<String, Object> toMap(Domain domain) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", domain.getName());
        map.put("dataType", domain.getDataType().name());
        map.put("length", domain.getLength());
        map.put("decimals", domain.getDecimals());
        map.put("description", domain.getDescription());
        if (domain.getValueRange() != null) {
            map.put("fixedValues", List.copyOf(domain.getValueRange().getFixedValues()));
        }
        return map;
    }
}
