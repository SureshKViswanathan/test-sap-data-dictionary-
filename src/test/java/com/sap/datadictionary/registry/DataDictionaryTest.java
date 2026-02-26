package com.sap.datadictionary.registry;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style test that exercises the full 3-Schema model
 * through the central {@link DataDictionary} registry.
 */
class DataDictionaryTest {

    private DataDictionary dd;

    @BeforeEach
    void setUp() {
        dd = new DataDictionary();
    }

    // ---- Internal Schema ----

    @Test
    void registerAndRetrieveDomain() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        dd.registerDomain(domain);

        assertSame(domain, dd.getDomain("ZCHAR10"));
        assertEquals(1, dd.getDomains().size());
    }

    @Test
    void duplicateDomainIsRejected() {
        Domain d1 = new Domain("ZCHAR10", DataType.CHAR, 10);
        Domain d2 = new Domain("ZCHAR10", DataType.CHAR, 20);
        dd.registerDomain(d1);

        assertThrows(IllegalArgumentException.class, () -> dd.registerDomain(d2));
    }

    @Test
    void registerAndRetrieveDataElement() {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        dd.registerDomain(domain);
        dd.registerDataElement(element);

        assertSame(element, dd.getDataElement("ZNAME"));
    }

    // ---- Conceptual Schema ----

    @Test
    void registerAndRetrieveTable() {
        TableDefinition table = new TableDefinition("ZCUSTOMER");
        dd.registerTable(table);

        assertSame(table, dd.getTable("ZCUSTOMER"));
    }

    @Test
    void registerAndRetrieveStructure() {
        Structure structure = new Structure("ZADDRESS");
        dd.registerStructure(structure);

        assertSame(structure, dd.getStructure("ZADDRESS"));
    }

    // ---- External Schema ----

    @Test
    void registerAndRetrieveView() {
        ViewDefinition view = new ViewDefinition("ZCUST_V",
                ViewDefinition.ViewType.DATABASE);
        dd.registerView(view);

        assertSame(view, dd.getView("ZCUST_V"));
    }

    @Test
    void registerAndRetrieveSearchHelp() {
        SearchHelp help = new SearchHelp("ZSH_CUST");
        dd.registerSearchHelp(help);

        assertSame(help, dd.getSearchHelp("ZSH_CUST"));
    }

    @Test
    void registerAndRetrieveLockObject() {
        TableDefinition table = new TableDefinition("ZCUSTOMER");
        LockObject lock = new LockObject("EZCUSTOMER", table);
        dd.registerTable(table);
        dd.registerLockObject(lock);

        assertSame(lock, dd.getLockObject("EZCUSTOMER"));
    }

    // ---- End-to-end scenario ----

    @Test
    void fullThreeSchemaScenario() {
        // 1. Internal Schema – Domains & Data Elements
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

        // 2. Conceptual Schema – Table
        TableDefinition custTable = new TableDefinition("ZCUSTOMER");
        custTable.setDescription("Customer master data");
        custTable.setDeliveryClass(TableDefinition.DeliveryClass.A);
        custTable.addField(new FieldDefinition("MANDT", mandtElement, true, false));
        custTable.addField(new FieldDefinition("NAME", nameElement, false, false));
        custTable.addField(new FieldDefinition("CURRENCY", currElement, false, true));

        dd.registerTable(custTable);

        // 3. External Schema – View, Search Help, Lock Object
        ViewDefinition view = new ViewDefinition("ZCUST_V",
                ViewDefinition.ViewType.PROJECTION);
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

        // Assertions across all three schemas
        assertEquals(3, dd.getDomains().size());
        assertEquals(3, dd.getDataElements().size());
        assertEquals(1, dd.getTables().size());
        assertEquals(1, dd.getViews().size());
        assertEquals(1, dd.getSearchHelps().size());
        assertEquals(1, dd.getLockObjects().size());

        // Verify value-range constraint propagation
        assertTrue(currencyDomain.getValueRange().isValid("EUR"));
        assertFalse(currencyDomain.getValueRange().isValid("GBP"));
    }
}
