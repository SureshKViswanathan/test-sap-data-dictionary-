package com.sap.datadictionary.patient;

import com.sap.datadictionary.registry.DataDictionary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for patient registration: DDIC schema initialisation
 * and patient CRUD REST endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PatientRegistrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataDictionary dictionary;

    // ── Schema initialisation ──────────────────────────────────────────────

    @Test
    void initializeSchemaCreatesAllDdicObjects() throws Exception {
        mockMvc.perform(post("/api/patients/schema/initialize"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        // Domains registered
        mockMvc.perform(get("/api/domains/ZPATIENT_ID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataType").value("NUMC"));

        mockMvc.perform(get("/api/domains/ZGENDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fixedValues", hasItems("M", "F", "O")));

        // Data elements registered
        mockMvc.perform(get("/api/data-elements/DE_PATIENT_ID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.longLabel").value("Patient Identifier"));

        // Table registered with 8 fields
        mockMvc.perform(get("/api/tables/ZPATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Patient Registration Table"))
                .andExpect(jsonPath("$.fields", hasSize(8)));

        // View registered
        mockMvc.perform(get("/api/views/ZPATIENT_LIST_V"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewType").value("PROJECTION"));

        // Search help registered
        mockMvc.perform(get("/api/search-helps/ZSH_PATIENT"))
                .andExpect(status().isOk());

        // Lock object registered
        mockMvc.perform(get("/api/lock-objects/EZPATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lockMode").value("EXCLUSIVE"));
    }

    @Test
    void initializeSchemaTwiceReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/patients/schema/initialize"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/patients/schema/initialize"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── Patient CRUD ──────────────────────────────────────────────────────

    @Test
    void listPatientsEmptyInitially() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void registerAndGetPatient() throws Exception {
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "firstName": "Jane",
                              "lastName": "Doe",
                              "dateOfBirth": "19900315",
                              "gender": "F",
                              "phone": "+1-555-0100",
                              "email": "jane.doe@example.com",
                              "address": "123 Main St"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").exists())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.dateOfBirth").value("19900315"))
                .andExpect(jsonPath("$.gender").value("F"))
                .andExpect(jsonPath("$.registeredAt").exists());

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getPatientById() throws Exception {
        String response = mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"firstName":"John","lastName":"Smith"}
                            """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract patientId – it is the first 10-digit value in the JSON
        String patientId = com.jayway.jsonpath.JsonPath.read(response, "$.patientId");

        mockMvc.perform(get("/api/patients/" + patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    void getUnknownPatientReturns404() throws Exception {
        mockMvc.perform(get("/api/patients/9999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerPatientWithMissingFirstNameFails() throws Exception {
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"lastName":"Doe"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void registerPatientWithMissingLastNameFails() throws Exception {
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"firstName":"Jane"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void multiplePatientRegistrations() throws Exception {
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"firstName\":\"Patient\",\"lastName\":\"" + i + "\"}"))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }
}
