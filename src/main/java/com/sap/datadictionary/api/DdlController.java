package com.sap.datadictionary.api;

import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.ddl.DdlGenerator;
import com.sap.datadictionary.ddl.SqlDialect;
import com.sap.datadictionary.external.ViewDefinition;
import com.sap.datadictionary.registry.DataDictionary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for DDL generation.
 */
@RestController
@RequestMapping("/api/ddl")
public class DdlController {

    private final DataDictionary dictionary;
    private final DdlGenerator ddlGenerator;

    public DdlController(DataDictionary dictionary, DdlGenerator ddlGenerator) {
        this.dictionary = dictionary;
        this.ddlGenerator = ddlGenerator;
    }

    @GetMapping("/tables/{name}")
    public ResponseEntity<Object> generateTableDdl(
            @PathVariable String name,
            @RequestParam(defaultValue = "POSTGRESQL") String dialect) {
        try {
            TableDefinition table = dictionary.getTable(name);
            if (table == null) {
                return ResponseEntity.notFound().build();
            }
            SqlDialect sqlDialect = SqlDialect.valueOf(dialect.toUpperCase());
            String ddl = ddlGenerator.generateCreateTable(table, sqlDialect);
            return ResponseEntity.ok(Map.of("ddl", ddl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/views/{name}")
    public ResponseEntity<Object> generateViewDdl(
            @PathVariable String name,
            @RequestParam(defaultValue = "POSTGRESQL") String dialect) {
        try {
            ViewDefinition view = dictionary.getView(name);
            if (view == null) {
                return ResponseEntity.notFound().build();
            }
            SqlDialect sqlDialect = SqlDialect.valueOf(dialect.toUpperCase());
            String ddl = ddlGenerator.generateCreateView(view, sqlDialect);
            return ResponseEntity.ok(Map.of("ddl", ddl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
