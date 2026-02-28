package com.sap.datadictionary.patient;

import java.time.LocalDateTime;

/**
 * Represents a registered patient in the patient registration system.
 * <p>
 * The fields in this record correspond to the DDIC table {@code ZPATIENT}
 * defined by {@link PatientRegistrationSchema}.
 * </p>
 */
public class PatientRecord {

    private final String patientId;
    private final String firstName;
    private final String lastName;
    private final String dateOfBirth;
    private final String gender;
    private String phone;
    private String email;
    private String address;
    private final LocalDateTime registeredAt;

    public PatientRecord(String patientId, String firstName, String lastName,
                         String dateOfBirth, String gender) {
        if (patientId == null || patientId.isBlank()) {
            throw new IllegalArgumentException("Patient ID must not be blank");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name must not be blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name must not be blank");
        }
        this.patientId = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.registeredAt = LocalDateTime.now();
    }

    public String getPatientId() {
        return patientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    @Override
    public String toString() {
        return "PatientRecord{patientId='" + patientId + "', firstName='" + firstName
                + "', lastName='" + lastName + "'}";
    }
}
