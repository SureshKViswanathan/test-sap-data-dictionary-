package com.sap.datadictionary.patient;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In-memory registry for patient records.
 * <p>
 * Generates sequential patient IDs in NUMC(10) format and stores
 * {@link PatientRecord}s, corresponding to rows in the DDIC table
 * {@code ZPATIENT} defined by {@link PatientRegistrationSchema}.
 * </p>
 */
public class PatientRegistry {

    private final Map<String, PatientRecord> patients = new LinkedHashMap<>();
    private int idSequence = 0;

    /**
     * Register a new patient and return the created record.
     * The patient ID is generated automatically as a zero-padded 10-digit number.
     */
    public synchronized PatientRecord register(String firstName, String lastName,
                                               String dateOfBirth, String gender,
                                               String phone, String email, String address) {
        String id = String.format("%010d", ++idSequence);
        PatientRecord record = new PatientRecord(id, firstName, lastName, dateOfBirth, gender);
        record.setPhone(phone);
        record.setEmail(email);
        record.setAddress(address);
        patients.put(id, record);
        return record;
    }

    /** Look up a patient by ID; returns {@code null} if not found. */
    public PatientRecord findById(String id) {
        return patients.get(id);
    }

    /** Return an unmodifiable snapshot of all registered patients. */
    public Map<String, PatientRecord> findAll() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(patients));
    }
}
