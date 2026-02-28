package com.sap.datadictionary.registry;

import com.sap.datadictionary.conceptual.FieldDefinition;
import com.sap.datadictionary.conceptual.Structure;
import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.external.SearchHelp;
import com.sap.datadictionary.external.ViewDefinition;
import com.sap.datadictionary.internal.DataElement;
import com.sap.datadictionary.internal.DataType;
import com.sap.datadictionary.internal.Domain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ConsistencyValidator} covering all four Milestone 3
 * deliverables:
 * <ol>
 *   <li>DataElement → Domain validation</li>
 *   <li>FieldDefinition → DataElement validation</li>
 *   <li>ViewDefinition → table-field validation</li>
 *   <li>Cycle / dependency detection</li>
 * </ol>
 */
class ConsistencyValidatorTest {

    private DataDictionary dd;

    @BeforeEach
    void setUp() {
        dd = new DataDictionary();
    }

    // ---- Constructor ----

    @Test
    void nullDictionaryIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new ConsistencyValidator(null));
    }

    // ---- Empty dictionary ----

    @Test
    void emptyDictionaryIsValid() {
        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
    }

    // ---- DataElement → Domain ----

    @Test
    void validDataElementPassesValidation() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        dd.registerDomain(domain);
        dd.registerDataElement(element);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertFalse(result.hasErrors());
    }

    @Test
    void dataElementWithUnregisteredDomainIsError() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        // Register element but NOT the domain
        dd.registerDataElement(element);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).message().contains("ZNAME"));
        assertTrue(result.getErrors().get(0).message().contains("ZCHAR10"));
    }

    @Test
    void dataElementWithDifferentDomainInstanceIsError() {
        Domain domain1 = new Domain("ZCHAR10", DataType.CHAR, 10);
        Domain domain2 = new Domain("ZCHAR10", DataType.CHAR, 20);
        DataElement element = new DataElement("ZNAME", domain1);
        dd.registerDomain(domain2);
        dd.registerDataElement(element);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().get(0).message().contains("differs"));
    }

    // ---- FieldDefinition → DataElement (tables) ----

    @Test
    void tableFieldWithRegisteredDataElementPassesValidation() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        dd.registerDomain(domain);
        dd.registerDataElement(element);

        TableDefinition table = new TableDefinition("ZCUSTOMER");
        table.addField(new FieldDefinition("NAME", element, false, false));
        dd.registerTable(table);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertFalse(result.hasErrors());
    }

    @Test
    void tableFieldWithUnregisteredDataElementIsError() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        dd.registerDomain(domain);
        // Do NOT register the DataElement

        TableDefinition table = new TableDefinition("ZCUSTOMER");
        table.addField(new FieldDefinition("NAME", element, false, false));
        dd.registerTable(table);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(f -> f.message().contains("ZCUSTOMER")
                        && f.message().contains("ZNAME")));
    }

    // ---- FieldDefinition → DataElement (structures) ----

    @Test
    void structureFieldWithUnregisteredDataElementIsError() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        dd.registerDomain(domain);

        Structure structure = new Structure("ZADDRESS");
        structure.addField(new FieldDefinition("STREET", element, false, false));
        dd.registerStructure(structure);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(f -> f.message().contains("ZADDRESS")
                        && f.message().contains("ZNAME")));
    }

    @Test
    void structureFieldWithRegisteredDataElementPassesValidation() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        dd.registerDomain(domain);
        dd.registerDataElement(element);

        Structure structure = new Structure("ZADDRESS");
        structure.addField(new FieldDefinition("STREET", element, false, false));
        dd.registerStructure(structure);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertFalse(result.hasErrors());
    }

    // ---- ViewDefinition → table fields ----

    @Test
    void viewWithValidFieldReferencesPassesValidation() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        dd.registerDomain(domain);
        dd.registerDataElement(element);

        TableDefinition table = new TableDefinition("ZCUSTOMER");
        table.addField(new FieldDefinition("NAME", element, false, false));
        dd.registerTable(table);

        ViewDefinition view = new ViewDefinition("ZCUST_V",
                ViewDefinition.ViewType.PROJECTION);
        view.addBaseTable(table);
        view.addSelectedField("NAME");
        dd.registerView(view);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertFalse(result.hasErrors());
    }

    @Test
    void viewSelectingNonExistentFieldIsError() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        dd.registerDomain(domain);
        dd.registerDataElement(element);

        TableDefinition table = new TableDefinition("ZCUSTOMER");
        table.addField(new FieldDefinition("NAME", element, false, false));
        dd.registerTable(table);

        ViewDefinition view = new ViewDefinition("ZCUST_V",
                ViewDefinition.ViewType.PROJECTION);
        view.addBaseTable(table);
        view.addSelectedField("DOES_NOT_EXIST");
        dd.registerView(view);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().get(0).message().contains("DOES_NOT_EXIST"));
        assertTrue(result.getErrors().get(0).message().contains("ZCUST_V"));
    }

    @Test
    void viewWithUnregisteredBaseTableIsError() {
        TableDefinition unregisteredTable = new TableDefinition("ZUNKNOWN");

        ViewDefinition view = new ViewDefinition("ZCUST_V",
                ViewDefinition.ViewType.DATABASE);
        view.addBaseTable(unregisteredTable);
        dd.registerView(view);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(f -> f.message().contains("ZUNKNOWN")
                        && f.message().contains("not registered")));
    }

    @Test
    void viewWithMultipleBaseTablesAggregatesFields() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement nameElem = new DataElement("ZNAME", domain);
        DataElement cityElem = new DataElement("ZCITY", domain);
        dd.registerDomain(domain);
        dd.registerDataElement(nameElem);
        dd.registerDataElement(cityElem);

        TableDefinition table1 = new TableDefinition("ZCUSTOMER");
        table1.addField(new FieldDefinition("NAME", nameElem, false, false));
        dd.registerTable(table1);

        TableDefinition table2 = new TableDefinition("ZADDRESS");
        table2.addField(new FieldDefinition("CITY", cityElem, false, false));
        dd.registerTable(table2);

        ViewDefinition view = new ViewDefinition("ZCUSTADDR_V",
                ViewDefinition.ViewType.DATABASE);
        view.addBaseTable(table1);
        view.addBaseTable(table2);
        view.addSelectedField("NAME");
        view.addSelectedField("CITY");
        dd.registerView(view);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertFalse(result.hasErrors());
    }

    // ---- SearchHelp → table fields ----

    @Test
    void searchHelpWithValidFieldsPassesValidation() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        dd.registerDomain(domain);
        dd.registerDataElement(element);

        TableDefinition table = new TableDefinition("ZCUSTOMER");
        table.addField(new FieldDefinition("NAME", element, false, false));
        dd.registerTable(table);

        SearchHelp help = new SearchHelp("ZSH_CUST");
        help.setSelectionMethod(table);
        help.addDisplayField("NAME");
        help.addExportField("NAME");
        dd.registerSearchHelp(help);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertFalse(result.hasErrors());
    }

    @Test
    void searchHelpWithInvalidDisplayFieldIsError() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        dd.registerDomain(domain);
        dd.registerDataElement(element);

        TableDefinition table = new TableDefinition("ZCUSTOMER");
        table.addField(new FieldDefinition("NAME", element, false, false));
        dd.registerTable(table);

        SearchHelp help = new SearchHelp("ZSH_CUST");
        help.setSelectionMethod(table);
        help.addDisplayField("NONEXISTENT");
        dd.registerSearchHelp(help);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().get(0).message().contains("NONEXISTENT"));
    }

    @Test
    void searchHelpWithInvalidExportFieldIsError() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        dd.registerDomain(domain);
        dd.registerDataElement(element);

        TableDefinition table = new TableDefinition("ZCUSTOMER");
        table.addField(new FieldDefinition("NAME", element, false, false));
        dd.registerTable(table);

        SearchHelp help = new SearchHelp("ZSH_CUST");
        help.setSelectionMethod(table);
        help.addExportField("INVALID");
        dd.registerSearchHelp(help);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().get(0).message().contains("INVALID"));
    }

    @Test
    void searchHelpWithNoSelectionMethodIsSkipped() {
        SearchHelp help = new SearchHelp("ZSH_CUST");
        dd.registerSearchHelp(help);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertFalse(result.hasErrors());
    }

    @Test
    void searchHelpWithUnregisteredTableIsError() {
        TableDefinition unregisteredTable = new TableDefinition("ZUNKNOWN");
        unregisteredTable.addField(new FieldDefinition("F1",
                new DataElement("ZDE", new Domain("ZD", DataType.CHAR, 5)),
                false, false));

        SearchHelp help = new SearchHelp("ZSH_TEST");
        help.setSelectionMethod(unregisteredTable);
        help.addDisplayField("F1");
        dd.registerSearchHelp(help);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(f -> f.message().contains("ZUNKNOWN")));
    }

    // ---- Dependency / completeness warnings ----

    @Test
    void viewWithNoBaseTablesProducesWarning() {
        ViewDefinition view = new ViewDefinition("ZEMPTY_V",
                ViewDefinition.ViewType.DATABASE);
        dd.registerView(view);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(f -> f.message().contains("no base tables")));
    }

    @Test
    void viewWithBaseTablesButNoFieldsProducesWarning() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        dd.registerDomain(domain);
        dd.registerDataElement(element);

        TableDefinition table = new TableDefinition("ZCUSTOMER");
        table.addField(new FieldDefinition("NAME", element, false, false));
        dd.registerTable(table);

        ViewDefinition view = new ViewDefinition("ZEMPTY_V",
                ViewDefinition.ViewType.DATABASE);
        view.addBaseTable(table);
        dd.registerView(view);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(f -> f.message().contains("selects no fields")));
    }

    @Test
    void tableWithNoFieldsProducesWarning() {
        TableDefinition table = new TableDefinition("ZEMPTY");
        dd.registerTable(table);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
                .anyMatch(f -> f.message().contains("no fields")));
    }

    // ---- Full 3-schema scenario ----

    @Test
    void fullyConsistentDictionaryPassesValidation() {
        // Internal
        Domain clientDomain = new Domain("MANDT", DataType.NUMC, 3);
        Domain nameDomain = new Domain("ZCHAR40", DataType.CHAR, 40);
        dd.registerDomain(clientDomain);
        dd.registerDomain(nameDomain);

        DataElement mandtElem = new DataElement("MANDT", clientDomain);
        DataElement nameElem = new DataElement("CUSTOMER_NAME", nameDomain);
        dd.registerDataElement(mandtElem);
        dd.registerDataElement(nameElem);

        // Conceptual
        TableDefinition custTable = new TableDefinition("ZCUSTOMER");
        custTable.addField(new FieldDefinition("MANDT", mandtElem, true, false));
        custTable.addField(new FieldDefinition("NAME", nameElem, false, false));
        dd.registerTable(custTable);

        // External
        ViewDefinition view = new ViewDefinition("ZCUST_V",
                ViewDefinition.ViewType.PROJECTION);
        view.addBaseTable(custTable);
        view.addSelectedField("NAME");
        dd.registerView(view);

        SearchHelp help = new SearchHelp("ZSH_CUST");
        help.setSelectionMethod(custTable);
        help.addDisplayField("NAME");
        help.addExportField("NAME");
        dd.registerSearchHelp(help);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertTrue(result.isValid(), "Expected no findings but got: " + result.getFindings());
    }

    @Test
    void multipleErrorsAreCollected() {
        // DataElement with unregistered Domain
        Domain d1 = new Domain("D1", DataType.CHAR, 5);
        DataElement e1 = new DataElement("E1", d1);
        dd.registerDataElement(e1);

        // Table field with unregistered DataElement
        Domain d2 = new Domain("D2", DataType.CHAR, 10);
        DataElement e2 = new DataElement("E2", d2);
        dd.registerDomain(d2);
        TableDefinition table = new TableDefinition("T1");
        table.addField(new FieldDefinition("F1", e2, false, false));
        dd.registerTable(table);

        // View selecting non-existent field
        ViewDefinition view = new ViewDefinition("V1",
                ViewDefinition.ViewType.DATABASE);
        view.addBaseTable(table);
        view.addSelectedField("GHOST");
        dd.registerView(view);

        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().size() >= 3,
                "Expected at least 3 errors but got: " + result.getErrors().size());
    }

    // ---- ValidationResult ----

    @Test
    void validationResultSeverityFiltering() {
        ValidationResult result = new ConsistencyValidator(dd).validate();
        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(0, result.getFindings().size());
    }
}
