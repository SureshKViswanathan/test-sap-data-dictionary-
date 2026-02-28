package com.sap.datadictionary.api;

import com.sap.datadictionary.patient.PatientRecord;
import com.sap.datadictionary.patient.PatientRegistry;
import com.sap.datadictionary.patient.PatientRegistrationSchema;
import com.sap.datadictionary.registry.DataDictionary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Patient Registration operations.
 * <p>
 * Provides two concerns:
 * <ol>
 *   <li>Schema initialisation – {@code POST /api/patients/schema/initialize}
 *       registers the patient DDIC objects (domains, data elements, table,
 *       view, search help and lock object) into the shared
 *       {@link DataDictionary}.</li>
 *   <li>Patient data CRUD – {@code POST/GET /api/patients} to register and
 *       retrieve patient records via the {@link PatientRegistry}.</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientRegistry patientRegistry;
    private final DataDictionary dictionary;

    public PatientController(PatientRegistry patientRegistry, DataDictionary dictionary) {
        this.patientRegistry = patientRegistry;
        this.dictionary = dictionary;
    }

    /**
     * Initialise the patient registration DDIC schema in the shared dictionary.
     * Idempotency: returns 400 if the schema has already been initialised.
     */
    @PostMapping("/schema/initialize")
    public ResponseEntity<Object> initializeSchema() {
        try {
            PatientRegistrationSchema.initialize(dictionary);
            return ResponseEntity.ok(Map.of("message",
                    "Patient registration schema initialised successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** List all registered patients. */
    @GetMapping
    public List<Map<String, Object>> list() {
        return patientRegistry.findAll().values().stream()
                .map(PatientController::toMap)
                .toList();
    }

    /** Get a single patient by ID; returns 404 if not found. */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String id) {
        PatientRecord record = patientRegistry.findById(id);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toMap(record));
    }

    /**
     * Register a new patient.
     * Required fields: {@code firstName}, {@code lastName}.
     * Optional fields: {@code dateOfBirth} (YYYYMMDD), {@code gender} (M/F/O),
     *   {@code phone}, {@code email}, {@code address}.
     */
    @PostMapping
    public ResponseEntity<Object> register(@RequestBody Map<String, Object> body) {
        try {
            String firstName   = (String) body.get("firstName");
            String lastName    = (String) body.get("lastName");
            String dateOfBirth = (String) body.getOrDefault("dateOfBirth", null);
            String gender      = (String) body.getOrDefault("gender", null);
            String phone       = (String) body.getOrDefault("phone", null);
            String email       = (String) body.getOrDefault("email", null);
            String address     = (String) body.getOrDefault("address", null);

            PatientRecord record = patientRegistry.register(
                    firstName, lastName, dateOfBirth, gender, phone, email, address);
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(record));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private static Map<String, Object> toMap(PatientRecord record) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("patientId",    record.getPatientId());
        map.put("firstName",    record.getFirstName());
        map.put("lastName",     record.getLastName());
        map.put("dateOfBirth",  record.getDateOfBirth());
        map.put("gender",       record.getGender());
        map.put("phone",        record.getPhone());
        map.put("email",        record.getEmail());
        map.put("address",      record.getAddress());
        map.put("registeredAt", record.getRegisteredAt().toString());
        return map;
    }
}
