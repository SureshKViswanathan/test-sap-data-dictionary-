package com.sap.datadictionary.api;

import com.sap.datadictionary.conceptual.FieldDefinition;
import com.sap.datadictionary.conceptual.TableDefinition;
import com.sap.datadictionary.external.SearchHelp;
import com.sap.datadictionary.external.ViewDefinition;
import com.sap.datadictionary.internal.DataElement;
import com.sap.datadictionary.internal.DataType;
import com.sap.datadictionary.internal.Domain;
import com.sap.datadictionary.registry.DataDictionary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the REST API controllers.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RestApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataDictionary dictionary;

    // ---- Domain endpoints ----

    @Test
    void listDomainsEmpty() throws Exception {
        mockMvc.perform(get("/api/domains"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createAndGetDomain() throws Exception {
        mockMvc.perform(post("/api/domains")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"ZCHAR10","dataType":"CHAR","length":10,"description":"Test domain"}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ZCHAR10"))
                .andExpect(jsonPath("$.dataType").value("CHAR"))
                .andExpect(jsonPath("$.length").value(10));

        mockMvc.perform(get("/api/domains/ZCHAR10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ZCHAR10"));
    }

    @Test
    void createDomainWithFixedValues() throws Exception {
        mockMvc.perform(post("/api/domains")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"WAERS","dataType":"CHAR","length":5,"fixedValues":["EUR","USD"]}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fixedValues", hasSize(2)));
    }

    @Test
    void getDomainNotFound() throws Exception {
        mockMvc.perform(get("/api/domains/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createDuplicateDomainFails() throws Exception {
        String json = """
            {"name":"ZCHAR10","dataType":"CHAR","length":10}
            """;
        mockMvc.perform(post("/api/domains")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/domains")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // ---- Data Element endpoints ----

    @Test
    void createAndGetDataElement() throws Exception {
        // First create a domain
        dictionary.registerDomain(new Domain("ZCHAR10", DataType.CHAR, 10));

        mockMvc.perform(post("/api/data-elements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"ZNAME","domainName":"ZCHAR10","shortLabel":"Name","longLabel":"Customer Name"}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ZNAME"))
                .andExpect(jsonPath("$.domainName").value("ZCHAR10"));

        mockMvc.perform(get("/api/data-elements/ZNAME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortLabel").value("Name"));
    }

    @Test
    void createDataElementWithMissingDomainFails() throws Exception {
        mockMvc.perform(post("/api/data-elements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"ZNAME","domainName":"NONEXISTENT"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Domain not found")));
    }

    // ---- Table endpoints ----

    @Test
    void createAndGetTable() throws Exception {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        dictionary.registerDomain(domain);
        dictionary.registerDataElement(element);

        mockMvc.perform(post("/api/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "tableName": "ZCUSTOMER",
                              "description": "Customer master",
                              "deliveryClass": "A",
                              "fields": [
                                {"fieldName": "NAME", "dataElementName": "ZNAME", "keyField": false, "nullable": false}
                              ]
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tableName").value("ZCUSTOMER"))
                .andExpect(jsonPath("$.fields", hasSize(1)));

        mockMvc.perform(get("/api/tables/ZCUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Customer master"));
    }

    // ---- View endpoints ----

    @Test
    void createAndGetView() throws Exception {
        TableDefinition table = new TableDefinition("ZCUSTOMER");
        dictionary.registerTable(table);

        mockMvc.perform(post("/api/views")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "viewName": "ZCUST_V",
                              "viewType": "DATABASE",
                              "baseTableNames": ["ZCUSTOMER"],
                              "selectedFields": ["NAME"]
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.viewName").value("ZCUST_V"));

        mockMvc.perform(get("/api/views/ZCUST_V"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewType").value("DATABASE"));
    }

    // ---- Structure endpoints ----

    @Test
    void createAndGetStructure() throws Exception {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        dictionary.registerDomain(domain);
        dictionary.registerDataElement(element);

        mockMvc.perform(post("/api/structures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "structureName": "ZADDRESS",
                              "description": "Address structure",
                              "fields": [
                                {"fieldName": "STREET", "dataElementName": "ZNAME", "keyField": false, "nullable": false}
                              ]
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.structureName").value("ZADDRESS"));

        mockMvc.perform(get("/api/structures/ZADDRESS"))
                .andExpect(status().isOk());
    }

    // ---- Search Help endpoints ----

    @Test
    void createAndGetSearchHelp() throws Exception {
        TableDefinition table = new TableDefinition("ZCUSTOMER");
        dictionary.registerTable(table);

        mockMvc.perform(post("/api/search-helps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "ZSH_CUST",
                              "selectionMethodName": "ZCUSTOMER",
                              "displayFields": ["NAME"],
                              "exportFields": ["NAME"]
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ZSH_CUST"));

        mockMvc.perform(get("/api/search-helps/ZSH_CUST"))
                .andExpect(status().isOk());
    }

    // ---- Lock Object endpoints ----

    @Test
    void createAndGetLockObject() throws Exception {
        TableDefinition table = new TableDefinition("ZCUSTOMER");
        dictionary.registerTable(table);

        mockMvc.perform(post("/api/lock-objects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "EZCUSTOMER",
                              "primaryTableName": "ZCUSTOMER",
                              "lockMode": "EXCLUSIVE"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("EZCUSTOMER"));

        mockMvc.perform(get("/api/lock-objects/EZCUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lockMode").value("EXCLUSIVE"));
    }

    // ---- Where-Used endpoints ----

    @Test
    void whereUsedDomain() throws Exception {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        dictionary.registerDomain(domain);
        dictionary.registerDataElement(element);

        TableDefinition table = new TableDefinition("ZCUSTOMER");
        table.addField(new FieldDefinition("NAME", element, false, false));
        dictionary.registerTable(table);

        mockMvc.perform(get("/api/where-used/domains/ZCHAR10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataElements", hasItem("ZNAME")))
                .andExpect(jsonPath("$.tables", hasItem("ZCUSTOMER")));
    }

    @Test
    void whereUsedTable() throws Exception {
        TableDefinition table = new TableDefinition("ZCUSTOMER");
        dictionary.registerTable(table);

        ViewDefinition view = new ViewDefinition("ZCUST_V", ViewDefinition.ViewType.DATABASE);
        view.addBaseTable(table);
        dictionary.registerView(view);

        SearchHelp help = new SearchHelp("ZSH_CUST");
        help.setSelectionMethod(table);
        dictionary.registerSearchHelp(help);

        mockMvc.perform(get("/api/where-used/tables/ZCUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.views", hasItem("ZCUST_V")))
                .andExpect(jsonPath("$.searchHelps", hasItem("ZSH_CUST")));
    }

    // ---- DDL endpoints ----

    @Test
    void generateTableDdl() throws Exception {
        Domain domain = new Domain("ZCHAR10", DataType.CHAR, 10);
        DataElement element = new DataElement("ZNAME", domain);
        dictionary.registerDomain(domain);
        dictionary.registerDataElement(element);

        TableDefinition table = new TableDefinition("ZCUSTOMER");
        table.addField(new FieldDefinition("NAME", element, true, false));
        dictionary.registerTable(table);

        mockMvc.perform(get("/api/ddl/tables/ZCUSTOMER?dialect=POSTGRESQL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ddl", containsString("CREATE TABLE ZCUSTOMER")));
    }

    @Test
    void generateDdlForUnknownTableReturns404() throws Exception {
        mockMvc.perform(get("/api/ddl/tables/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    // ---- Static content ----

    @Test
    void indexPageIsServed() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SAP Data Dictionary Browser")));
    }
}
