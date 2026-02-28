package com.sap.datadictionary.api;

import com.sap.datadictionary.registry.WhereUsedAnalyzer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for where-used analysis.
 * <p>
 * Provides endpoints to find all objects that reference a given
 * Domain, Data Element, or Table across the Data Dictionary.
 * </p>
 */
@RestController
@RequestMapping("/api/where-used")
public class WhereUsedController {

    private final WhereUsedAnalyzer analyzer;

    public WhereUsedController(WhereUsedAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    /**
     * Find all objects using a given Domain (across all layers).
     */
    @GetMapping("/domains/{name}")
    public ResponseEntity<Map<String, List<String>>> domainUsages(@PathVariable String name) {
        Map<String, List<String>> result = analyzer.findAllUsagesOfDomain(name);
        return ResponseEntity.ok(result);
    }

    /**
     * Find all Tables and Structures using a given Data Element.
     */
    @GetMapping("/data-elements/{name}")
    public ResponseEntity<Map<String, List<String>>> dataElementUsages(@PathVariable String name) {
        Map<String, List<String>> result = analyzer.findTablesUsingDataElement(name);
        return ResponseEntity.ok(result);
    }

    /**
     * Find all Views, Search Helps, and Lock Objects that reference a given Table.
     */
    @GetMapping("/tables/{name}")
    public ResponseEntity<Map<String, List<String>>> tableUsages(@PathVariable String name) {
        Map<String, List<String>> result = analyzer.findUsagesOfTable(name);
        return ResponseEntity.ok(result);
    }
}
