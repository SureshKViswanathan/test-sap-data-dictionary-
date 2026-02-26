# SAP Data Dictionary – Java Reference Implementation

A Java 21 / Gradle project that models **SAP's Data Dictionary (DDIC)** based on the
**ANSI/SPARC 3-Schema Architecture**.

---

## ANSI/SPARC 3-Schema Architecture Overview

The ANSI/SPARC model separates a database system into three abstraction layers so
that changes in one layer do not ripple into the others:

```
┌─────────────────────────────────────────────────┐
│           External Schema (User Views)          │  ← ViewDefinition, SearchHelp, LockObject
│  How different users / applications see data    │
├─────────────────────────────────────────────────┤
│         Conceptual Schema (Logical Model)       │  ← TableDefinition, FieldDefinition, Structure
│  Unified logical structure of all data          │
├─────────────────────────────────────────────────┤
│       Internal Schema (Physical / Storage)      │  ← Domain, DataElement, ValueRange, DataType
│  Data types, lengths, value ranges, storage     │
└─────────────────────────────────────────────────┘
```

### How SAP DDIC Maps to the 3-Schema Model

| ANSI/SPARC Layer | SAP DDIC Object | Java Class |
|---|---|---|
| **Internal** | Domain | `com.sap.datadictionary.internal.Domain` |
| **Internal** | Data Type (CHAR, NUMC …) | `com.sap.datadictionary.internal.DataType` |
| **Internal** | Value Range (fixed values) | `com.sap.datadictionary.internal.ValueRange` |
| **Internal → Conceptual** | Data Element | `com.sap.datadictionary.internal.DataElement` |
| **Conceptual** | Table | `com.sap.datadictionary.conceptual.TableDefinition` |
| **Conceptual** | Field | `com.sap.datadictionary.conceptual.FieldDefinition` |
| **Conceptual** | Structure | `com.sap.datadictionary.conceptual.Structure` |
| **External** | Database / Projection View | `com.sap.datadictionary.external.ViewDefinition` |
| **External** | Search Help | `com.sap.datadictionary.external.SearchHelp` |
| **External** | Lock Object | `com.sap.datadictionary.external.LockObject` |
| **Registry** | Data Dictionary (catalog) | `com.sap.datadictionary.registry.DataDictionary` |

---

## Project Structure

```
src/
├── main/java/com/sap/datadictionary/
│   ├── internal/          ← Internal Schema (Domain, DataType, ValueRange, DataElement)
│   ├── conceptual/        ← Conceptual Schema (TableDefinition, FieldDefinition, Structure)
│   ├── external/          ← External Schema (ViewDefinition, SearchHelp, LockObject)
│   └── registry/          ← Central DataDictionary registry
└── test/java/com/sap/datadictionary/
    ├── internal/          ← Unit tests for Internal Schema
    ├── conceptual/        ← Unit tests for Conceptual Schema
    ├── external/          ← Unit tests for External Schema
    └── registry/          ← Integration tests (full 3-schema scenario)
```

---

## Prerequisites

* **Java 21** (project uses Java toolchain — Gradle auto-provisions if needed)

## Build & Test

```bash
# Build the project
./gradlew build

# Run tests only
./gradlew test
```

---

## Development Plan – Initial Phase (Milestones)

### Milestone 1 ✅ – Core Domain Model (Current)
> Establish the foundational classes for all three ANSI/SPARC layers.

| Deliverable | Status |
|---|---|
| Internal Schema: `Domain`, `DataType`, `ValueRange`, `DataElement` | ✅ Done |
| Conceptual Schema: `TableDefinition`, `FieldDefinition`, `Structure` | ✅ Done |
| External Schema: `ViewDefinition`, `SearchHelp`, `LockObject` | ✅ Done |
| Central `DataDictionary` registry | ✅ Done |
| Unit + integration tests (JUnit 5) | ✅ Done |
| Gradle build with Java 21 toolchain | ✅ Done |

### Milestone 2 – Persistence & Serialization
> Persist dictionary metadata so definitions survive restarts.

| Deliverable | Status |
|---|---|
| JSON/YAML serialization of all DDIC objects | ⬜ Planned |
| File-based repository (load/save dictionary to disk) | ⬜ Planned |
| Import/export of dictionary definitions | ⬜ Planned |

### Milestone 3 – Validation & Consistency Checks
> Ensure referential integrity across the three schema layers.

| Deliverable | Status |
|---|---|
| Validate that every `DataElement` references a registered `Domain` | ⬜ Planned |
| Validate that every `FieldDefinition` references a registered `DataElement` | ⬜ Planned |
| Validate that every `ViewDefinition` only references existing table fields | ⬜ Planned |
| Cycle / dependency detection | ⬜ Planned |

### Milestone 4 – DDL Generation
> Generate SQL DDL from the conceptual schema.

| Deliverable | Status |
|---|---|
| SQL `CREATE TABLE` generation from `TableDefinition` | ⬜ Planned |
| SQL `CREATE VIEW` generation from `ViewDefinition` | ⬜ Planned |
| Dialect support (PostgreSQL, H2, SAP HANA) | ⬜ Planned |

### Milestone 5 – REST API & UI
> Expose the Data Dictionary via a lightweight service.

| Deliverable | Status |
|---|---|
| REST API for CRUD operations on DDIC objects | ⬜ Planned |
| Simple web UI for browsing the dictionary | ⬜ Planned |
| Where-used analysis (find all tables using a given Domain) | ⬜ Planned |

---

## License

This project is provided as an example / reference implementation.