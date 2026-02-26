package com.sap.datadictionary.conceptual;

import com.sap.datadictionary.internal.DataElement;
import com.sap.datadictionary.internal.DataType;
import com.sap.datadictionary.internal.Domain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableDefinitionTest {

    private DataElement clientElement;
    private DataElement nameElement;

    @BeforeEach
    void setUp() {
        Domain charDomain = new Domain("ZCHAR10", DataType.CHAR, 10);
        Domain clientDomain = new Domain("MANDT", DataType.NUMC, 3);
        clientElement = new DataElement("MANDT", clientDomain);
        nameElement = new DataElement("ZNAME", charDomain);
    }

    @Test
    void createTableWithFields() {
        TableDefinition table = new TableDefinition("ZCUSTOMER");
        table.addField(new FieldDefinition("MANDT", clientElement, true, false));
        table.addField(new FieldDefinition("NAME", nameElement, false, true));

        assertEquals("ZCUSTOMER", table.getTableName());
        assertEquals(2, table.getFields().size());
        assertEquals(1, table.getKeyFields().size());
        assertEquals("MANDT", table.getKeyFields().get(0).getFieldName());
    }

    @Test
    void fieldLookupByName() {
        TableDefinition table = new TableDefinition("ZCUSTOMER");
        table.addField(new FieldDefinition("MANDT", clientElement, true, false));

        assertNotNull(table.getField("MANDT"));
        assertNull(table.getField("NONEXISTENT"));
    }

    @Test
    void duplicateFieldIsRejected() {
        TableDefinition table = new TableDefinition("ZCUSTOMER");
        table.addField(new FieldDefinition("MANDT", clientElement, true, false));

        assertThrows(IllegalArgumentException.class,
                () -> table.addField(new FieldDefinition("MANDT", clientElement, true, false)));
    }

    @Test
    void deliveryClassDefaultsToA() {
        TableDefinition table = new TableDefinition("ZCUSTOMER");

        assertEquals(TableDefinition.DeliveryClass.A, table.getDeliveryClass());
    }

    @Test
    void fieldsListIsUnmodifiable() {
        TableDefinition table = new TableDefinition("ZCUSTOMER");

        assertThrows(UnsupportedOperationException.class,
                () -> table.getFields().add(
                        new FieldDefinition("X", nameElement, false, true)));
    }

    @Test
    void blankTableNameIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new TableDefinition(""));
    }
}
