package com.sap.datadictionary.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the outcome of a consistency validation run on a {@link DataDictionary}.
 * <p>
 * Each finding is classified as either an {@link Severity#ERROR} (a broken
 * reference that would cause failures) or a {@link Severity#WARNING}
 * (a potential issue that may deserve attention).
 * </p>
 */
public class ValidationResult {

    /** Severity level for a single validation finding. */
    public enum Severity { ERROR, WARNING }

    /**
     * A single validation finding.
     *
     * @param severity  ERROR or WARNING
     * @param message   human-readable description of the problem
     */
    public record Finding(Severity severity, String message) {}

    private final List<Finding> findings = new ArrayList<>();

    void addError(String message) {
        findings.add(new Finding(Severity.ERROR, message));
    }

    void addWarning(String message) {
        findings.add(new Finding(Severity.WARNING, message));
    }

    /** Returns {@code true} when no ERRORs or WARNINGs were recorded. */
    public boolean isValid() {
        return findings.isEmpty();
    }

    /** Returns {@code true} when at least one ERROR was recorded. */
    public boolean hasErrors() {
        return findings.stream().anyMatch(f -> f.severity() == Severity.ERROR);
    }

    /** Returns {@code true} when at least one WARNING was recorded. */
    public boolean hasWarnings() {
        return findings.stream().anyMatch(f -> f.severity() == Severity.WARNING);
    }

    /** Returns an unmodifiable list of all findings. */
    public List<Finding> getFindings() {
        return Collections.unmodifiableList(findings);
    }

    /** Returns only the ERROR findings. */
    public List<Finding> getErrors() {
        return findings.stream()
                .filter(f -> f.severity() == Severity.ERROR)
                .toList();
    }

    /** Returns only the WARNING findings. */
    public List<Finding> getWarnings() {
        return findings.stream()
                .filter(f -> f.severity() == Severity.WARNING)
                .toList();
    }

    @Override
    public String toString() {
        return "ValidationResult{errors=" + getErrors().size()
                + ", warnings=" + getWarnings().size() + '}';
    }
}
