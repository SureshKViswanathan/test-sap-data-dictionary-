package com.sap.datadictionary.patient;

import com.sap.datadictionary.conceptual.FieldDefinition;
import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.external.LockObject;
import com.sap.datadictionary.external.SearchHelp;
import com.sap.datadictionary.external.ViewDefinition;
import com.sap.datadictionary.internal.DataElement;
import com.sap.datadictionary.internal.DataType;
import com.sap.datadictionary.internal.Domain;
import com.sap.datadictionary.internal.ValueRange;
import com.sap.datadictionary.registry.DataDictionary;

/**
 * Defines the patient registration schema in the SAP Data Dictionary,
 * covering all three ANSI/SPARC layers:
 * <ul>
 *   <li><b>Internal</b> – {@link Domain}s for each patient attribute type</li>
 *   <li><b>Conceptual</b> – {@link DataElement}s and the {@code ZPATIENT} table</li>
 *   <li><b>External</b> – a projection view, a search help, and a lock object</li>
 * </ul>
 *
 * Call {@link #initialize(DataDictionary)} once to register all objects into a
 * given {@link DataDictionary} instance.
 */
public class PatientRegistrationSchema {

    private PatientRegistrationSchema() {
        // utility class – do not instantiate
    }

    /**
     * Register all patient registration DDIC objects into {@code dictionary}.
     *
     * @throws IllegalArgumentException if any object is already registered
     */
    public static void initialize(DataDictionary dictionary) {

        // ── Internal Schema: Domains ──────────────────────────────────────────

        Domain patientIdDomain = new Domain("ZPATIENT_ID", DataType.NUMC, 10);
        patientIdDomain.setDescription("Patient identifier");
        dictionary.registerDomain(patientIdDomain);

        Domain name40Domain = new Domain("ZNAME_40", DataType.CHAR, 40);
        name40Domain.setDescription("Name field (40 characters)");
        dictionary.registerDomain(name40Domain);

        Domain dobDomain = new Domain("ZDOB", DataType.DATE, 8);
        dobDomain.setDescription("Date of birth (YYYYMMDD)");
        dictionary.registerDomain(dobDomain);

        Domain genderDomain = new Domain("ZGENDER", DataType.CHAR, 1);
        genderDomain.setDescription("Gender code (M/F/O)");
        ValueRange genderRange = new ValueRange();
        genderRange.addFixedValue("M");
        genderRange.addFixedValue("F");
        genderRange.addFixedValue("O");
        genderDomain.setValueRange(genderRange);
        dictionary.registerDomain(genderDomain);

        Domain phone20Domain = new Domain("ZPHONE_20", DataType.CHAR, 20);
        phone20Domain.setDescription("Phone number");
        dictionary.registerDomain(phone20Domain);

        Domain email100Domain = new Domain("ZEMAIL_100", DataType.STRING, 100);
        email100Domain.setDescription("Email address");
        dictionary.registerDomain(email100Domain);

        Domain address100Domain = new Domain("ZADDRESS_100", DataType.CHAR, 100);
        address100Domain.setDescription("Postal address");
        dictionary.registerDomain(address100Domain);

        // ── Internal → Conceptual: Data Elements ─────────────────────────────

        DataElement patientIdElem = new DataElement("DE_PATIENT_ID", patientIdDomain);
        patientIdElem.setShortLabel("Pat. ID");
        patientIdElem.setMediumLabel("Patient ID");
        patientIdElem.setLongLabel("Patient Identifier");
        dictionary.registerDataElement(patientIdElem);

        DataElement firstNameElem = new DataElement("DE_FIRST_NAME", name40Domain);
        firstNameElem.setShortLabel("First Nm");
        firstNameElem.setMediumLabel("First Name");
        firstNameElem.setLongLabel("Patient First Name");
        dictionary.registerDataElement(firstNameElem);

        DataElement lastNameElem = new DataElement("DE_LAST_NAME", name40Domain);
        lastNameElem.setShortLabel("Last Nm");
        lastNameElem.setMediumLabel("Last Name");
        lastNameElem.setLongLabel("Patient Last Name");
        dictionary.registerDataElement(lastNameElem);

        DataElement dobElem = new DataElement("DE_DATE_OF_BIRTH", dobDomain);
        dobElem.setShortLabel("DOB");
        dobElem.setMediumLabel("Date of Birth");
        dobElem.setLongLabel("Patient Date of Birth");
        dictionary.registerDataElement(dobElem);

        DataElement genderElem = new DataElement("DE_GENDER", genderDomain);
        genderElem.setShortLabel("Gender");
        genderElem.setMediumLabel("Gender");
        genderElem.setLongLabel("Patient Gender");
        dictionary.registerDataElement(genderElem);

        DataElement phoneElem = new DataElement("DE_PHONE", phone20Domain);
        phoneElem.setShortLabel("Phone");
        phoneElem.setMediumLabel("Phone Number");
        phoneElem.setLongLabel("Contact Phone Number");
        dictionary.registerDataElement(phoneElem);

        DataElement emailElem = new DataElement("DE_EMAIL", email100Domain);
        emailElem.setShortLabel("Email");
        emailElem.setMediumLabel("Email");
        emailElem.setLongLabel("Email Address");
        dictionary.registerDataElement(emailElem);

        DataElement addressElem = new DataElement("DE_ADDRESS", address100Domain);
        addressElem.setShortLabel("Address");
        addressElem.setMediumLabel("Address");
        addressElem.setLongLabel("Patient Address");
        dictionary.registerDataElement(addressElem);

        // ── Conceptual Schema: Table ──────────────────────────────────────────

        TableDefinition patientTable = new TableDefinition("ZPATIENT");
        patientTable.setDescription("Patient Registration Table");
        patientTable.setDeliveryClass(TableDefinition.DeliveryClass.A);
        patientTable.addField(new FieldDefinition("PATIENT_ID",    patientIdElem, true,  false));
        patientTable.addField(new FieldDefinition("FIRST_NAME",    firstNameElem, false, false));
        patientTable.addField(new FieldDefinition("LAST_NAME",     lastNameElem,  false, false));
        patientTable.addField(new FieldDefinition("DATE_OF_BIRTH", dobElem,       false, true));
        patientTable.addField(new FieldDefinition("GENDER",        genderElem,    false, true));
        patientTable.addField(new FieldDefinition("PHONE",         phoneElem,     false, true));
        patientTable.addField(new FieldDefinition("EMAIL",         emailElem,     false, true));
        patientTable.addField(new FieldDefinition("ADDRESS",       addressElem,   false, true));
        dictionary.registerTable(patientTable);

        // ── External Schema: Projection View ─────────────────────────────────

        ViewDefinition listView = new ViewDefinition(
                "ZPATIENT_LIST_V", ViewDefinition.ViewType.PROJECTION);
        listView.setDescription("Patient List View (key fields only)");
        listView.addBaseTable(patientTable);
        listView.addSelectedField("PATIENT_ID");
        listView.addSelectedField("FIRST_NAME");
        listView.addSelectedField("LAST_NAME");
        listView.addSelectedField("DATE_OF_BIRTH");
        listView.addSelectedField("GENDER");
        dictionary.registerView(listView);

        // ── External Schema: Search Help ──────────────────────────────────────

        SearchHelp searchHelp = new SearchHelp("ZSH_PATIENT");
        searchHelp.setDescription("Patient Search Help");
        searchHelp.setSelectionMethod(patientTable);
        searchHelp.addDisplayField("PATIENT_ID");
        searchHelp.addDisplayField("FIRST_NAME");
        searchHelp.addDisplayField("LAST_NAME");
        searchHelp.addExportField("PATIENT_ID");
        dictionary.registerSearchHelp(searchHelp);

        // ── External Schema: Lock Object ──────────────────────────────────────

        LockObject lockObject = new LockObject("EZPATIENT", patientTable);
        lockObject.setLockMode(LockObject.LockMode.EXCLUSIVE);
        lockObject.setDescription("Patient Record Lock");
        dictionary.registerLockObject(lockObject);
    }
}
