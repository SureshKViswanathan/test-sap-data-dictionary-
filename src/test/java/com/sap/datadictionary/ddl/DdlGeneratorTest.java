package com.sap.datadictionary.ddl;

import com.sap.datadictionary.conceptual.FieldDefinition;
import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.external.ViewDefinition;
import com.sap.datadictionary.internal.DataElement;
import com.sap.datadictionary.internal.DataType;
import com.sap.datadictionary.internal.Domain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DdlGenerator} covering Milestone 4 deliverables:
 * <ol>
 *   <li>SQL {@code CREATE TABLE} generation from {@link TableDefinition}</li>
 *   <li>SQL {@code CREATE VIEW} generation from {@link ViewDefinition}</li>
 *   <li>Dialect support (PostgreSQL, H2, SAP HANA)</li>
 * </ol>
 */
class DdlGeneratorTest {

    private DdlGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new DdlGenerator();
    }

    // ---- Helpers ----

    private Domain domain(String name, DataType type, int length) {
        return new Domain(name, type, length);
    }

    private Domain domainWithDecimals(String name, DataType type, int length, int decimals) {
        return new Domain(name, type, length, decimals);
    }

    private DataElement element(String name, Domain domain) {
        return new DataElement(name, domain);
    }

    private FieldDefinition field(String name, DataElement de, boolean key, boolean nullable) {
        return new FieldDefinition(name, de, key, nullable);
    }

    // ---- generateCreateTable – argument validation ----

    @Test
    void nullTableIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> generator.generateCreateTable(null, SqlDialect.POSTGRESQL));
    }

    @Test
    void nullDialectIsRejectedForTable() {
        TableDefinition table = new TableDefinition("T1");
        table.addField(field("ID", element("ID_DE", domain("D", DataType.CHAR, 5)), true, false));
        assertThrows(IllegalArgumentException.class,
                () -> generator.generateCreateTable(table, null));
    }

    @Test
    void emptyTableIsRejected() {
        TableDefinition table = new TableDefinition("EMPTY");
        assertThrows(IllegalArgumentException.class,
                () -> generator.generateCreateTable(table, SqlDialect.POSTGRESQL));
    }

    // ---- generateCreateTable – PostgreSQL ----

    @Test
    void createTableSingleKeyFieldPostgresql() {
        Domain d = domain("ZCHAR5", DataType.CHAR, 5);
        DataElement de = element("MANDT", d);
        TableDefinition table = new TableDefinition("ZCLIENT");
        table.addField(field("MANDT", de, true, false));

        String ddl = generator.generateCreateTable(table, SqlDialect.POSTGRESQL);

        assertTrue(ddl.startsWith("CREATE TABLE ZCLIENT ("));
        assertTrue(ddl.contains("MANDT CHAR(5) NOT NULL"));
        assertTrue(ddl.contains("PRIMARY KEY (MANDT)"));
    }

    @Test
    void createTableMultipleFieldsPostgresql() {
        Domain charDomain = domain("ZCHAR40", DataType.CHAR, 40);
        Domain numcDomain = domain("MANDT_D", DataType.NUMC, 3);

        DataElement mandtDe = element("MANDT", numcDomain);
        DataElement nameDe = element("NAME", charDomain);
        DataElement cityDe = element("CITY", charDomain);

        TableDefinition table = new TableDefinition("ZCUSTOMER");
        table.addField(field("MANDT", mandtDe, true, false));
        table.addField(field("NAME", nameDe, false, false));
        table.addField(field("CITY", cityDe, false, true));

        String ddl = generator.generateCreateTable(table, SqlDialect.POSTGRESQL);

        assertTrue(ddl.contains("MANDT CHAR(3) NOT NULL,"));
        assertTrue(ddl.contains("NAME CHAR(40) NOT NULL,"));
        // nullable field has no NOT NULL
        assertTrue(ddl.contains("CITY CHAR(40),"));
        assertTrue(ddl.contains("PRIMARY KEY (MANDT)"));
    }

    @Test
    void createTableDecimalFieldPostgresql() {
        Domain decDomain = domainWithDecimals("AMOUNT", DataType.DECIMAL, 15, 2);
        DataElement de = element("AMOUNT_DE", decDomain);
        TableDefinition table = new TableDefinition("ZORDER");
        table.addField(field("AMOUNT", de, false, true));

        String ddl = generator.generateCreateTable(table, SqlDialect.POSTGRESQL);

        assertTrue(ddl.contains("AMOUNT DECIMAL(15, 2)"));
        assertFalse(ddl.contains("PRIMARY KEY"));
    }

    @Test
    void createTableAllDataTypesPostgresql() {
        TableDefinition table = new TableDefinition("ZALL");
        table.addField(field("C1", element("DE_CHAR",      domain("D_CHAR",  DataType.CHAR,      10)), false, true));
        table.addField(field("C2", element("DE_STR",       domain("D_STR",   DataType.STRING,    50)), false, true));
        table.addField(field("C3", element("DE_NUMC",      domain("D_NUMC",  DataType.NUMC,       8)), false, true));
        table.addField(field("C4", element("DE_INT",       domain("D_INT",   DataType.INTEGER,    4)), false, true));
        table.addField(field("C5", element("DE_DEC",       domainWithDecimals("D_DEC", DataType.DECIMAL, 10, 3)), false, true));
        table.addField(field("C6", element("DE_DATE",      domain("D_DATE",  DataType.DATE,       8)), false, true));
        table.addField(field("C7", element("DE_TIME",      domain("D_TIME",  DataType.TIME,       6)), false, true));
        table.addField(field("C8", element("DE_TSTAMP",    domain("D_TSTAMP",DataType.TIMESTAMP, 15)), false, true));
        table.addField(field("C9", element("DE_RAW",       domain("D_RAW",   DataType.RAW,       16)), false, true));

        String ddl = generator.generateCreateTable(table, SqlDialect.POSTGRESQL);

        assertTrue(ddl.contains("C1 CHAR(10)"));
        assertTrue(ddl.contains("C2 VARCHAR(50)"));
        assertTrue(ddl.contains("C3 CHAR(8)"));
        assertTrue(ddl.contains("C4 INTEGER"));
        assertTrue(ddl.contains("C5 DECIMAL(10, 3)"));
        assertTrue(ddl.contains("C6 DATE"));
        assertTrue(ddl.contains("C7 TIME"));
        assertTrue(ddl.contains("C8 TIMESTAMP"));
        assertTrue(ddl.contains("C9 BYTEA"));
    }

    // ---- generateCreateTable – H2 ----

    @Test
    void createTableH2UsesVarcharAndBinary() {
        Domain strDomain = domain("ZSTR", DataType.STRING, 100);
        Domain rawDomain = domain("ZRAW",  DataType.RAW,    20);
        DataElement strDe = element("STR_DE", strDomain);
        DataElement rawDe = element("RAW_DE", rawDomain);

        TableDefinition table = new TableDefinition("ZH2TABLE");
        table.addField(field("TXT",  strDe, false, true));
        table.addField(field("DATA", rawDe, false, true));

        String ddl = generator.generateCreateTable(table, SqlDialect.H2);

        assertTrue(ddl.contains("TXT VARCHAR(100)"));
        assertTrue(ddl.contains("DATA BINARY(20)"));
    }

    // ---- generateCreateTable – SAP HANA ----

    @Test
    void createTableHanaUsesNcharAndNvarchar() {
        Domain charDomain = domain("ZCHAR", DataType.CHAR,   10);
        Domain strDomain  = domain("ZSTR",  DataType.STRING, 50);
        Domain numcDomain = domain("ZNUMC", DataType.NUMC,    5);
        Domain rawDomain  = domain("ZRAW",  DataType.RAW,    32);

        TableDefinition table = new TableDefinition("ZHANATABLE");
        table.addField(field("C1", element("DE_C", charDomain), false, true));
        table.addField(field("C2", element("DE_S", strDomain),  false, true));
        table.addField(field("C3", element("DE_N", numcDomain), false, true));
        table.addField(field("C4", element("DE_R", rawDomain),  false, true));

        String ddl = generator.generateCreateTable(table, SqlDialect.HANA);

        assertTrue(ddl.contains("C1 NCHAR(10)"),    "CHAR should map to NCHAR in HANA");
        assertTrue(ddl.contains("C2 NVARCHAR(50)"), "STRING should map to NVARCHAR in HANA");
        assertTrue(ddl.contains("C3 NCHAR(5)"),     "NUMC should map to NCHAR in HANA");
        assertTrue(ddl.contains("C4 VARBINARY(32)"), "RAW should map to VARBINARY in HANA");
    }

    @Test
    void createTableHanaCommonTypesUnchanged() {
        TableDefinition table = new TableDefinition("ZHANA2");
        table.addField(field("I1", element("DE_I",  domain("D_I",  DataType.INTEGER,   4)), false, true));
        table.addField(field("D1", element("DE_D",  domainWithDecimals("D_D", DataType.DECIMAL, 15, 2)), false, true));
        table.addField(field("DT", element("DE_DT", domain("D_DT", DataType.DATE,      8)), false, true));
        table.addField(field("TM", element("DE_TM", domain("D_TM", DataType.TIME,      6)), false, true));
        table.addField(field("TS", element("DE_TS", domain("D_TS", DataType.TIMESTAMP,15)), false, true));

        String ddl = generator.generateCreateTable(table, SqlDialect.HANA);

        assertTrue(ddl.contains("I1 INTEGER"));
        assertTrue(ddl.contains("D1 DECIMAL(15, 2)"));
        assertTrue(ddl.contains("DT DATE"));
        assertTrue(ddl.contains("TM TIME"));
        assertTrue(ddl.contains("TS TIMESTAMP"));
    }

    // ---- generateCreateTable – composite primary key ----

    @Test
    void createTableCompositePrimaryKey() {
        Domain d = domain("ZCHAR10", DataType.CHAR, 10);
        DataElement de1 = element("DE1", d);
        DataElement de2 = element("DE2", d);
        DataElement de3 = element("DE3", d);

        TableDefinition table = new TableDefinition("ZCOMP");
        table.addField(field("K1", de1, true,  false));
        table.addField(field("K2", de2, true,  false));
        table.addField(field("V1", de3, false, true));

        String ddl = generator.generateCreateTable(table, SqlDialect.POSTGRESQL);

        assertTrue(ddl.contains("PRIMARY KEY (K1, K2)"));
    }

    // ---- generateCreateTable – no primary key ----

    @Test
    void createTableNoPrimaryKeyOmitsConstraint() {
        Domain d = domain("ZCHAR10", DataType.CHAR, 10);
        DataElement de = element("DE", d);
        TableDefinition table = new TableDefinition("ZNOPK");
        table.addField(field("COL1", de, false, true));
        table.addField(field("COL2", de, false, true));

        String ddl = generator.generateCreateTable(table, SqlDialect.POSTGRESQL);

        assertFalse(ddl.contains("PRIMARY KEY"), "No primary key clause expected");
        // Last field should NOT end with a comma
        assertTrue(ddl.contains("COL2 CHAR(10)\n)"));
    }

    // ---- generateCreateView – argument validation ----

    @Test
    void nullViewIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> generator.generateCreateView(null, SqlDialect.POSTGRESQL));
    }

    @Test
    void nullDialectIsRejectedForView() {
        Domain d = domain("D", DataType.CHAR, 5);
        DataElement de = element("DE", d);
        TableDefinition table = new TableDefinition("T");
        table.addField(field("F", de, false, true));

        ViewDefinition view = new ViewDefinition("V", ViewDefinition.ViewType.PROJECTION);
        view.addBaseTable(table);
        assertThrows(IllegalArgumentException.class,
                () -> generator.generateCreateView(view, null));
    }

    @Test
    void viewWithNoBaseTablesIsRejected() {
        ViewDefinition view = new ViewDefinition("VEMPTY", ViewDefinition.ViewType.DATABASE);
        assertThrows(IllegalArgumentException.class,
                () -> generator.generateCreateView(view, SqlDialect.POSTGRESQL));
    }

    // ---- generateCreateView – single base table ----

    @Test
    void createViewSingleTableSelectedFields() {
        Domain d = domain("ZCHAR40", DataType.CHAR, 40);
        DataElement nameDe = element("NAME_DE", d);
        DataElement cityDe = element("CITY_DE", d);

        TableDefinition table = new TableDefinition("ZCUSTOMER");
        table.addField(field("NAME", nameDe, false, false));
        table.addField(field("CITY", cityDe, false, true));

        ViewDefinition view = new ViewDefinition("ZCUST_V", ViewDefinition.ViewType.PROJECTION);
        view.addBaseTable(table);
        view.addSelectedField("NAME");
        view.addSelectedField("CITY");

        String ddl = generator.generateCreateView(view, SqlDialect.POSTGRESQL);

        assertEquals("CREATE VIEW ZCUST_V AS\nSELECT NAME, CITY\nFROM ZCUSTOMER", ddl);
    }

    @Test
    void createViewNoSelectedFieldsGeneratesSelectStar() {
        Domain d = domain("ZCHAR10", DataType.CHAR, 10);
        DataElement de = element("DE", d);
        TableDefinition table = new TableDefinition("ZTABLE");
        table.addField(field("F1", de, false, true));

        ViewDefinition view = new ViewDefinition("ZSTAR_V", ViewDefinition.ViewType.PROJECTION);
        view.addBaseTable(table);

        String ddl = generator.generateCreateView(view, SqlDialect.POSTGRESQL);

        assertEquals("CREATE VIEW ZSTAR_V AS\nSELECT *\nFROM ZTABLE", ddl);
    }

    // ---- generateCreateView – multiple base tables ----

    @Test
    void createViewMultipleBaseTables() {
        Domain d = domain("ZCHAR20", DataType.CHAR, 20);
        DataElement de1 = element("DE1", d);
        DataElement de2 = element("DE2", d);

        TableDefinition t1 = new TableDefinition("ZTABLE1");
        t1.addField(field("COL_A", de1, false, true));

        TableDefinition t2 = new TableDefinition("ZTABLE2");
        t2.addField(field("COL_B", de2, false, true));

        ViewDefinition view = new ViewDefinition("ZJOIN_V", ViewDefinition.ViewType.DATABASE);
        view.addBaseTable(t1);
        view.addBaseTable(t2);
        view.addSelectedField("COL_A");
        view.addSelectedField("COL_B");

        String ddl = generator.generateCreateView(view, SqlDialect.POSTGRESQL);

        assertTrue(ddl.startsWith("CREATE VIEW ZJOIN_V AS\n"));
        assertTrue(ddl.contains("SELECT COL_A, COL_B\n"));
        assertTrue(ddl.contains("FROM ZTABLE1, ZTABLE2"));
    }

    // ---- generateCreateView – dialect does not affect view syntax ----

    @Test
    void createViewSameSyntaxAcrossDialects() {
        Domain d = domain("ZCHAR10", DataType.CHAR, 10);
        DataElement de = element("DE", d);
        TableDefinition table = new TableDefinition("ZTAB");
        table.addField(field("COL", de, false, true));

        ViewDefinition view = new ViewDefinition("ZVIEW", ViewDefinition.ViewType.PROJECTION);
        view.addBaseTable(table);
        view.addSelectedField("COL");

        String expected = "CREATE VIEW ZVIEW AS\nSELECT COL\nFROM ZTAB";

        assertEquals(expected, generator.generateCreateView(view, SqlDialect.POSTGRESQL));
        assertEquals(expected, generator.generateCreateView(view, SqlDialect.H2));
        assertEquals(expected, generator.generateCreateView(view, SqlDialect.HANA));
    }

    // ---- generateCreateView – all ViewType variants ----

    @Test
    void createViewDatabaseType() {
        Domain d = domain("D", DataType.CHAR, 5);
        DataElement de = element("DE", d);
        TableDefinition table = new TableDefinition("T");
        table.addField(field("F", de, false, true));

        ViewDefinition view = new ViewDefinition("VDBVIEW", ViewDefinition.ViewType.DATABASE);
        view.addBaseTable(table);
        view.addSelectedField("F");

        String ddl = generator.generateCreateView(view, SqlDialect.H2);
        assertTrue(ddl.startsWith("CREATE VIEW VDBVIEW AS\n"));
    }

    @Test
    void createViewMaintenanceType() {
        Domain d = domain("D", DataType.CHAR, 5);
        DataElement de = element("DE", d);
        TableDefinition table = new TableDefinition("T");
        table.addField(field("F", de, false, true));

        ViewDefinition view = new ViewDefinition("VMAINT", ViewDefinition.ViewType.MAINTENANCE);
        view.addBaseTable(table);
        view.addSelectedField("F");

        String ddl = generator.generateCreateView(view, SqlDialect.HANA);
        assertTrue(ddl.startsWith("CREATE VIEW VMAINT AS\n"));
    }

    // ---- toSqlType – spot checks ----

    @Test
    void toSqlTypeRawPostgresqlIsBytea() {
        Domain d = domain("ZRAW", DataType.RAW, 10);
        assertEquals("BYTEA", generator.toSqlType(d, SqlDialect.POSTGRESQL));
    }

    @Test
    void toSqlTypeRawH2IsBinary() {
        Domain d = domain("ZRAW", DataType.RAW, 10);
        assertEquals("BINARY(10)", generator.toSqlType(d, SqlDialect.H2));
    }

    @Test
    void toSqlTypeRawHanaIsVarbinary() {
        Domain d = domain("ZRAW", DataType.RAW, 10);
        assertEquals("VARBINARY(10)", generator.toSqlType(d, SqlDialect.HANA));
    }

    @Test
    void toSqlTypeDecimalIncludesScale() {
        Domain d = domainWithDecimals("ZDEC", DataType.DECIMAL, 18, 4);
        assertEquals("DECIMAL(18, 4)", generator.toSqlType(d, SqlDialect.POSTGRESQL));
        assertEquals("DECIMAL(18, 4)", generator.toSqlType(d, SqlDialect.H2));
        assertEquals("DECIMAL(18, 4)", generator.toSqlType(d, SqlDialect.HANA));
    }
}
