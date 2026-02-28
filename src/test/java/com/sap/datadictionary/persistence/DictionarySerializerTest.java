package com.sap.datadictionary.persistence;

import com.sap.datadictionary.conceptual.FieldDefinition;
import com.sap.datadictionary.conceptual.Structure;
import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.external.LockObject;
import com.sap.datadictionary.external.SearchHelp;
import com.sap.datadictionary.external.ViewDefinition;
import com.sap.datadictionary.internal.DataElement;
import com.sap.datadictionary.internal.DataType;
import com.sap.datadictionary.internal.Domain;
import com.sap.datadictionary.internal.ValueRange;
import com.sap.datadictionary.registry.DataDictionary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DictionarySerializer} JSON round-trip serialization.
 */
class DictionarySerializerTest {

    private DictionarySerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new DictionarySerializer();
    }

    @Test
    void emptyDictionaryRoundTrip() {
        DataDictionary original = new DataDictionary();
        String json = serializer.toJson(original);
        DataDictionary restored = serializer.fromJson(json);

        assertTrue(restored.getDomains().isEmpty());
        assertTrue(restored.getDataElements().isEmpty());
        assertTrue(restored.getTables().isEmpty());
        assertTrue(restored.getStructures().isEmpty());
        assertTrue(restored.getViews().isEmpty());
        assertTrue(restored.getSearchHelps().isEmpty());
        assertTrue(restored.getLockObjects().isEmpty());
    }

    @Test
    void domainRoundTrip() {
        DataDictionary original = new DataDictionary();
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        domain.setDescription("Ten char field");
        original.registerDomain(domain);

        DataDictionary restored = serializer.fromJson(serializer.toJson(original));

        Domain d = restored.getDomain("ZCHAR10");
        assertNotNull(d);
        assertEquals("ZCHAR10", d.getName());
        assertEquals(DataType.CHAR, d.getDataType());
        assertEquals(10, d.getLength());
        assertEquals(0, d.getDecimals());
        assertEquals("Ten char field", d.getDescription());
    }

    @Test
    void domainWithValueRangeRoundTrip() {
        DataDictionary original = new DataDictionary();
        Domain domain = new Domain("WAERS", DataType.CHAR, 5);
        ValueRange vr = new ValueRange();
        vr.addFixedValue("EUR");
        vr.addFixedValue("USD");
        domain.setValueRange(vr);
        original.registerDomain(domain);

        DataDictionary restored = serializer.fromJson(serializer.toJson(original));

        Domain d = restored.getDomain("WAERS");
        assertNotNull(d.getValueRange());
        assertTrue(d.getValueRange().isValid("EUR"));
        assertTrue(d.getValueRange().isValid("USD"));
        assertFalse(d.getValueRange().isValid("GBP"));
    }

    @Test
    void domainWithDecimalsRoundTrip() {
        DataDictionary original = new DataDictionary();
        original.registerDomain(new Domain("ZAMOUNT", DataType.DECIMAL, 15, 2));

        DataDictionary restored = serializer.fromJson(serializer.toJson(original));

        Domain d = restored.getDomain("ZAMOUNT");
        assertEquals(DataType.DECIMAL, d.getDataType());
        assertEquals(15, d.getLength());
        assertEquals(2, d.getDecimals());
    }

    @Test
    void dataElementRoundTrip() {
        DataDictionary original = new DataDictionary();
        Domain domain = new Domain("ZCHAR40", DataType.CHAR, 40);
        original.registerDomain(domain);

        DataElement element = new DataElement("ZNAME", domain);
        element.setShortLabel("Name");
        element.setMediumLabel("Cust. Name");
        element.setLongLabel("Customer Name");
        element.setDocumentation("Full name of the customer");
        original.registerDataElement(element);

        DataDictionary restored = serializer.fromJson(serializer.toJson(original));

        DataElement de = restored.getDataElement("ZNAME");
        assertNotNull(de);
        assertEquals("ZNAME", de.getName());
        assertEquals("ZCHAR40", de.getDomain().getName());
        assertEquals("Name", de.getShortLabel());
        assertEquals("Cust. Name", de.getMediumLabel());
        assertEquals("Customer Name", de.getLongLabel());
        assertEquals("Full name of the customer", de.getDocumentation());
    }

    @Test
    void tableDefinitionRoundTrip() {
        DataDictionary original = buildFullDictionary();

        DataDictionary restored = serializer.fromJson(serializer.toJson(original));

        TableDefinition table = restored.getTable("ZCUSTOMER");
        assertNotNull(table);
        assertEquals("Customer master data", table.getDescription());
        assertEquals(TableDefinition.DeliveryClass.A, table.getDeliveryClass());
        assertFalse(table.isBuffered());
        assertEquals(3, table.getFields().size());

        FieldDefinition keyField = table.getField("MANDT");
        assertTrue(keyField.isKeyField());
        assertFalse(keyField.isNullable());
        assertEquals("MANDT", keyField.getDataElement().getName());
    }

    @Test
    void structureRoundTrip() {
        DataDictionary original = new DataDictionary();
        Domain dom = new Domain("ZCHAR20", DataType.CHAR, 20);
        original.registerDomain(dom);
        DataElement de = new DataElement("ZSTREET", dom);
        original.registerDataElement(de);

        Structure structure = new Structure("ZADDRESS");
        structure.setDescription("Address structure");
        structure.addField(new FieldDefinition("STREET", de, false, true));
        original.registerStructure(structure);

        DataDictionary restored = serializer.fromJson(serializer.toJson(original));

        Structure s = restored.getStructure("ZADDRESS");
        assertNotNull(s);
        assertEquals("Address structure", s.getDescription());
        assertEquals(1, s.getFields().size());
        assertEquals("STREET", s.getField("STREET").getFieldName());
    }

    @Test
    void viewDefinitionRoundTrip() {
        DataDictionary original = buildFullDictionary();

        DataDictionary restored = serializer.fromJson(serializer.toJson(original));

        ViewDefinition view = restored.getView("ZCUST_V");
        assertNotNull(view);
        assertEquals(ViewDefinition.ViewType.PROJECTION, view.getViewType());
        assertEquals(1, view.getBaseTables().size());
        assertEquals("ZCUSTOMER", view.getBaseTables().get(0).getTableName());
        assertEquals(2, view.getSelectedFields().size());
    }

    @Test
    void searchHelpRoundTrip() {
        DataDictionary original = buildFullDictionary();

        DataDictionary restored = serializer.fromJson(serializer.toJson(original));

        SearchHelp help = restored.getSearchHelp("ZSH_CUST");
        assertNotNull(help);
        assertEquals("ZCUSTOMER", help.getSelectionMethod().getTableName());
        assertEquals(1, help.getDisplayFields().size());
        assertEquals(1, help.getExportFields().size());
    }

    @Test
    void lockObjectRoundTrip() {
        DataDictionary original = buildFullDictionary();

        DataDictionary restored = serializer.fromJson(serializer.toJson(original));

        LockObject lock = restored.getLockObject("EZCUSTOMER");
        assertNotNull(lock);
        assertEquals("ZCUSTOMER", lock.getPrimaryTable().getTableName());
        assertEquals(LockObject.LockMode.EXCLUSIVE, lock.getLockMode());
        assertTrue(lock.getSecondaryTables().isEmpty());
    }

    @Test
    void fullThreeSchemaRoundTrip() {
        DataDictionary original = buildFullDictionary();

        String json = serializer.toJson(original);
        DataDictionary restored = serializer.fromJson(json);

        assertEquals(original.getDomains().size(), restored.getDomains().size());
        assertEquals(original.getDataElements().size(), restored.getDataElements().size());
        assertEquals(original.getTables().size(), restored.getTables().size());
        assertEquals(original.getViews().size(), restored.getViews().size());
        assertEquals(original.getSearchHelps().size(), restored.getSearchHelps().size());
        assertEquals(original.getLockObjects().size(), restored.getLockObjects().size());

        // Verify cross-layer references are intact
        DataElement nameElem = restored.getDataElement("CUSTOMER_NAME");
        assertSame(restored.getDomain("ZCHAR40"), nameElem.getDomain());

        FieldDefinition nameField = restored.getTable("ZCUSTOMER").getField("NAME");
        assertSame(nameElem, nameField.getDataElement());
    }

    @Test
    void invalidJsonThrowsException() {
        assertThrows(DictionarySerializationException.class,
                () -> serializer.fromJson("not valid json"));
    }

    @Test
    void missingDomainReferenceThrowsException() {
        // JSON with a DataElement that references a non-existent Domain
        String json = """
                {
                  "domains": {},
                  "dataElements": {
                    "ZNAME": {
                      "name": "ZNAME",
                      "domainName": "MISSING_DOMAIN",
                      "shortLabel": null,
                      "mediumLabel": null,
                      "longLabel": null,
                      "documentation": null
                    }
                  },
                  "tables": {},
                  "structures": {},
                  "views": {},
                  "searchHelps": {},
                  "lockObjects": {}
                }
                """;
        assertThrows(DictionarySerializationException.class,
                () -> serializer.fromJson(json));
    }

    // ---- helper ----

    /** Build a full 3-schema dictionary (same as DataDictionaryTest.fullThreeSchemaScenario). */
    static DataDictionary buildFullDictionary() {
        DataDictionary dd = new DataDictionary();

        // Internal Schema
        Domain clientDomain = new Domain("MANDT", DataType.NUMC, 3);
        Domain nameDomain = new Domain("ZCHAR40", DataType.CHAR, 40);
        Domain currencyDomain = new Domain("WAERS", DataType.CHAR, 5);
        ValueRange currencyRange = new ValueRange();
        currencyRange.addFixedValue("EUR");
        currencyRange.addFixedValue("USD");
        currencyDomain.setValueRange(currencyRange);

        dd.registerDomain(clientDomain);
        dd.registerDomain(nameDomain);
        dd.registerDomain(currencyDomain);

        DataElement mandtElement = new DataElement("MANDT", clientDomain);
        mandtElement.setShortLabel("Clnt");
        DataElement nameElement = new DataElement("CUSTOMER_NAME", nameDomain);
        nameElement.setShortLabel("Name");
        nameElement.setLongLabel("Customer Name");
        DataElement currElement = new DataElement("CURRENCY", currencyDomain);
        dd.registerDataElement(mandtElement);
        dd.registerDataElement(nameElement);
        dd.registerDataElement(currElement);

        // Conceptual Schema
        TableDefinition custTable = new TableDefinition("ZCUSTOMER");
        custTable.setDescription("Customer master data");
        custTable.setDeliveryClass(TableDefinition.DeliveryClass.A);
        custTable.addField(new FieldDefinition("MANDT", mandtElement, true, false));
        custTable.addField(new FieldDefinition("NAME", nameElement, false, false));
        custTable.addField(new FieldDefinition("CURRENCY", currElement, false, true));
        dd.registerTable(custTable);

        // External Schema
        ViewDefinition view = new ViewDefinition("ZCUST_V", ViewDefinition.ViewType.PROJECTION);
        view.addBaseTable(custTable);
        view.addSelectedField("NAME");
        view.addSelectedField("CURRENCY");
        dd.registerView(view);

        SearchHelp help = new SearchHelp("ZSH_CUST");
        help.setSelectionMethod(custTable);
        help.addDisplayField("NAME");
        help.addExportField("NAME");
        dd.registerSearchHelp(help);

        LockObject lock = new LockObject("EZCUSTOMER", custTable);
        lock.setLockMode(LockObject.LockMode.EXCLUSIVE);
        dd.registerLockObject(lock);

        return dd;
    }
}
