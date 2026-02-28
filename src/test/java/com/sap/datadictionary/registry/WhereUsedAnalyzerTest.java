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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WhereUsedAnalyzer}.
 */
class WhereUsedAnalyzerTest {

    private DataDictionary dd;
    private WhereUsedAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        dd = new DataDictionary();
        analyzer = new WhereUsedAnalyzer(dd);

        // Set up a small dictionary
        Domain charDomain = new Domain("ZCHAR10", DataType.CHAR, 10);
        Domain numcDomain = new Domain("ZNUMC3", DataType.NUMC, 3);
        dd.registerDomain(charDomain);
        dd.registerDomain(numcDomain);

        DataElement nameElement = new DataElement("ZNAME", charDomain);
        DataElement idElement = new DataElement("ZID", numcDomain);
        DataElement codeElement = new DataElement("ZCODE", charDomain);
        dd.registerDataElement(nameElement);
        dd.registerDataElement(idElement);
        dd.registerDataElement(codeElement);

        TableDefinition custTable = new TableDefinition("ZCUSTOMER");
        custTable.addField(new FieldDefinition("ID", idElement, true, false));
        custTable.addField(new FieldDefinition("NAME", nameElement, false, false));
        dd.registerTable(custTable);

        TableDefinition orderTable = new TableDefinition("ZORDER");
        orderTable.addField(new FieldDefinition("ORDER_ID", idElement, true, false));
        orderTable.addField(new FieldDefinition("CODE", codeElement, false, true));
        dd.registerTable(orderTable);

        Structure addressStruct = new Structure("ZADDRESS");
        addressStruct.addField(new FieldDefinition("STREET", nameElement, false, false));
        dd.registerStructure(addressStruct);

        ViewDefinition view = new ViewDefinition("ZCUST_V", ViewDefinition.ViewType.DATABASE);
        view.addBaseTable(custTable);
        view.addSelectedField("NAME");
        dd.registerView(view);

        SearchHelp help = new SearchHelp("ZSH_CUST");
        help.setSelectionMethod(custTable);
        help.addDisplayField("NAME");
        dd.registerSearchHelp(help);

        LockObject lock = new LockObject("EZCUSTOMER", custTable);
        lock.addSecondaryTable(orderTable);
        dd.registerLockObject(lock);
    }

    @Test
    void findDataElementsUsingDomain() {
        List<String> result = analyzer.findDataElementsUsingDomain("ZCHAR10");
        assertEquals(2, result.size());
        assertTrue(result.contains("ZNAME"));
        assertTrue(result.contains("ZCODE"));
    }

    @Test
    void findDataElementsUsingDomainReturnsEmptyForUnknown() {
        List<String> result = analyzer.findDataElementsUsingDomain("UNKNOWN");
        assertTrue(result.isEmpty());
    }

    @Test
    void findTablesUsingDataElement() {
        Map<String, List<String>> result = analyzer.findTablesUsingDataElement("ZID");
        assertEquals(List.of("ZCUSTOMER", "ZORDER"), result.get("tables"));
        assertNull(result.get("structures"));
    }

    @Test
    void findTablesAndStructuresUsingDataElement() {
        Map<String, List<String>> result = analyzer.findTablesUsingDataElement("ZNAME");
        assertEquals(List.of("ZCUSTOMER"), result.get("tables"));
        assertEquals(List.of("ZADDRESS"), result.get("structures"));
    }

    @Test
    void findUsagesOfTable() {
        Map<String, List<String>> result = analyzer.findUsagesOfTable("ZCUSTOMER");
        assertEquals(List.of("ZCUST_V"), result.get("views"));
        assertEquals(List.of("ZSH_CUST"), result.get("searchHelps"));
        assertEquals(List.of("EZCUSTOMER"), result.get("lockObjects"));
    }

    @Test
    void findUsagesOfTableAsSecondary() {
        Map<String, List<String>> result = analyzer.findUsagesOfTable("ZORDER");
        assertEquals(List.of("EZCUSTOMER"), result.get("lockObjects"));
        assertNull(result.get("views"));
    }

    @Test
    void findAllUsagesOfDomain() {
        Map<String, List<String>> result = analyzer.findAllUsagesOfDomain("ZCHAR10");
        assertEquals(2, result.get("dataElements").size());
        assertTrue(result.get("tables").contains("ZCUSTOMER"));
        assertTrue(result.get("tables").contains("ZORDER"));
        assertEquals(List.of("ZADDRESS"), result.get("structures"));
    }

    @Test
    void findAllUsagesOfUnusedDomainReturnsEmpty() {
        Domain unusedDomain = new Domain("ZUNUSED", DataType.DATE, 8);
        dd.registerDomain(unusedDomain);

        Map<String, List<String>> result = analyzer.findAllUsagesOfDomain("ZUNUSED");
        assertTrue(result.isEmpty());
    }

    @Test
    void constructorRejectsNullDictionary() {
        assertThrows(IllegalArgumentException.class, () -> new WhereUsedAnalyzer(null));
    }
}
