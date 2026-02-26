package com.sap.datadictionary.internal;

/**
 * Represents an SAP Data Dictionary Domain.
 * <p>
 * In the ANSI/SPARC Internal Schema, a Domain defines the technical
 * attributes of a field: its data type, length, decimal places, and
 * allowed value range. Domains are the lowest building block and
 * correspond to the physical storage characteristics.
 * </p>
 */
public class Domain {

    private final String name;
    private final DataType dataType;
    private final int length;
    private final int decimals;
    private String description;
    private ValueRange valueRange;

    public Domain(String name, DataType dataType, int length) {
        this(name, dataType, length, 0);
    }

    public Domain(String name, DataType dataType, int length, int decimals) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Domain name must not be blank");
        }
        if (dataType == null) {
            throw new IllegalArgumentException("Data type must not be null");
        }
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        if (decimals < 0) {
            throw new IllegalArgumentException("Decimals must not be negative");
        }
        this.name = name;
        this.dataType = dataType;
        this.length = length;
        this.decimals = decimals;
    }

    public String getName() {
        return name;
    }

    public DataType getDataType() {
        return dataType;
    }

    public int getLength() {
        return length;
    }

    public int getDecimals() {
        return decimals;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ValueRange getValueRange() {
        return valueRange;
    }

    public void setValueRange(ValueRange valueRange) {
        this.valueRange = valueRange;
    }

    @Override
    public String toString() {
        return "Domain{name='" + name + "', type=" + dataType
                + ", length=" + length + ", decimals=" + decimals + '}';
    }
}
