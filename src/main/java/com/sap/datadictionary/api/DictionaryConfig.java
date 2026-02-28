package com.sap.datadictionary.api;

import com.sap.datadictionary.ddl.DdlGenerator;
import com.sap.datadictionary.patient.PatientRegistry;
import com.sap.datadictionary.registry.DataDictionary;
import com.sap.datadictionary.registry.WhereUsedAnalyzer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that provides the shared {@link DataDictionary}
 * instance and related services as beans.
 */
@Configuration
public class DictionaryConfig {

    @Bean
    public DataDictionary dataDictionary() {
        return new DataDictionary();
    }

    @Bean
    public DdlGenerator ddlGenerator() {
        return new DdlGenerator();
    }

    @Bean
    public WhereUsedAnalyzer whereUsedAnalyzer(DataDictionary dataDictionary) {
        return new WhereUsedAnalyzer(dataDictionary);
    }

    @Bean
    public PatientRegistry patientRegistry() {
        return new PatientRegistry();
    }
}
