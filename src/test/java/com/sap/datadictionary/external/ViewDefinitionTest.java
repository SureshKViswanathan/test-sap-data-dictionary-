package com.sap.datadictionary.external;

import com.sap.datadictionary.conceptual.FieldDefinition;
import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.internal.DataElement;
import com.sap.datadictionary.internal.DataType;
import com.sap.datadictionary.internal.Domain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ViewDefinitionTest {

    private TableDefinition customerTable;

    @BeforeEach
    void setUp() {
        Domain charDomain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement nameElement = new DataElement("ZNAME", charDomain);

        customerTable = new TableDefinition("ZCUSTOMER");
        customerTable.addField(new FieldDefinition("NAME", nameElement, false, true));
    }

    @Test
    void createDatabaseView() {
        ViewDefinition view = new ViewDefinition("ZCUST_V", ViewDefinition.ViewType.DATABASE);
        view.addBaseTable(customerTable);
        view.addSelectedField("NAME");

        assertEquals("ZCUST_V", view.getViewName());
        assertEquals(ViewDefinition.ViewType.DATABASE, view.getViewType());
        assertEquals(1, view.getBaseTables().size());
        assertEquals(1, view.getSelectedFields().size());
    }

    @Test
    void blankViewNameIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new ViewDefinition("", ViewDefinition.ViewType.DATABASE));
    }

    @Test
    void nullViewTypeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new ViewDefinition("TEST", null));
    }

    @Test
    void baseTablesListIsUnmodifiable() {
        ViewDefinition view = new ViewDefinition("ZCUST_V", ViewDefinition.ViewType.DATABASE);

        assertThrows(UnsupportedOperationException.class,
                () -> view.getBaseTables().add(customerTable));
    }
}
