package com.sap.datadictionary.external;

import com.sap.datadictionary.conceptual.FieldDefinition;
import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.internal.DataElement;
import com.sap.datadictionary.internal.DataType;
import com.sap.datadictionary.internal.Domain;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchHelpTest {

    @Test
    void createSearchHelp() {
        Domain charDomain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement nameElement = new DataElement("ZNAME", charDomain);

        TableDefinition table = new TableDefinition("ZCUSTOMER");
        table.addField(new FieldDefinition("NAME", nameElement, false, true));

        SearchHelp help = new SearchHelp("ZSH_CUST");
        help.setSelectionMethod(table);
        help.addDisplayField("NAME");
        help.addExportField("NAME");

        assertEquals("ZSH_CUST", help.getName());
        assertSame(table, help.getSelectionMethod());
        assertEquals(1, help.getDisplayFields().size());
        assertEquals(1, help.getExportFields().size());
    }

    @Test
    void blankNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SearchHelp(""));
    }

    @Test
    void blankDisplayFieldIsRejected() {
        SearchHelp help = new SearchHelp("ZSH");

        assertThrows(IllegalArgumentException.class,
                () -> help.addDisplayField(""));
    }
}
