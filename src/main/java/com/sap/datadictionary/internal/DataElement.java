package com.sap.datadictionary.internal;

/**
 * Represents an SAP Data Dictionary Data Element.
 * <p>
 * A Data Element bridges the Internal and Conceptual schemas. It
 * references a {@link Domain} for its technical (physical) properties
 * while adding semantic metadata such as field labels and documentation
 * that are used at the conceptual level.
 * </p>
 */
public class DataElement {

    private final String name;
    private final Domain domain;
    private String shortLabel;
    private String mediumLabel;
    private String longLabel;
    private String documentation;

    public DataElement(String name, Domain domain) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Data element name must not be blank");
        }
        if (domain == null) {
            throw new IllegalArgumentException("Domain must not be null");
        }
        this.name = name;
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public Domain getDomain() {
        return domain;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public void setShortLabel(String shortLabel) {
        this.shortLabel = shortLabel;
    }

    public String getMediumLabel() {
        return mediumLabel;
    }

    public void setMediumLabel(String mediumLabel) {
        this.mediumLabel = mediumLabel;
    }

    public String getLongLabel() {
        return longLabel;
    }

    public void setLongLabel(String longLabel) {
        this.longLabel = longLabel;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    @Override
    public String toString() {
        return "DataElement{name='" + name + "', domain=" + domain.getName() + '}';
    }
}
