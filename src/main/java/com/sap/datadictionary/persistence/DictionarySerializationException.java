package com.sap.datadictionary.persistence;

/**
 * Thrown when serialization or deserialization of a Data Dictionary fails.
 */
public class DictionarySerializationException extends RuntimeException {

    public DictionarySerializationException(String message) {
        super(message);
    }

    public DictionarySerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
