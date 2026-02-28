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
│   ├── registry/          ← Central DataDictionary registry
│   ├── ddl/               ← DDL Generation (DdlGenerator, SqlDialect)
│   └── api/               ← REST API Controllers
├── main/resources/
│   ├── static/            ← Web UI (index.html)
│   └── application.properties
└── test/java/com/sap/datadictionary/
    ├── internal/          ← Unit tests for Internal Schema
    ├── conceptual/        ← Unit tests for Conceptual Schema
    ├── external/          ← Unit tests for External Schema
    ├── registry/          ← Integration tests (full 3-schema scenario)
    ├── ddl/               ← Unit tests for DDL generation
    └── api/               ← REST API integration tests
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

### Milestone 2 ✅ – Persistence & Serialization
> Persist dictionary metadata so definitions survive restarts.

| Deliverable | Status |
|---|---|
| JSON serialization of all DDIC objects (`DictionarySerializer`) | ✅ Done |
| File-based repository (load/save dictionary to disk) (`DictionaryRepository`) | ✅ Done |
| Import/export of dictionary definitions | ✅ Done |

### Milestone 3 ✅ – Validation & Consistency Checks
> Ensure referential integrity across the three schema layers.

| Deliverable | Status |
|---|---|
| Validate that every `DataElement` references a registered `Domain` | ✅ Done |
| Validate that every `FieldDefinition` references a registered `DataElement` | ✅ Done |
| Validate that every `ViewDefinition` only references existing table fields | ✅ Done |
| Cycle / dependency detection | ✅ Done |

### Milestone 4 ✅ – DDL Generation
> Generate SQL DDL from the conceptual schema.

| Deliverable | Status |
|---|---|
| SQL `CREATE TABLE` generation from `TableDefinition` | ✅ Done |
| SQL `CREATE VIEW` generation from `ViewDefinition` | ✅ Done |
| Dialect support (PostgreSQL, H2, SAP HANA) | ✅ Done |

### Milestone 5 ✅ – REST API & UI
> Expose the Data Dictionary via a lightweight service.

| Deliverable | Status |
|---|---|
| REST API for CRUD operations on DDIC objects | ✅ Done |
| Simple web UI for browsing the dictionary | ✅ Done |
| Where-used analysis (find all tables using a given Domain) | ✅ Done |

---

## REST API Endpoints

Once the application is running (`./gradlew bootRun`), the following endpoints are available at `http://localhost:8080`:

### DDIC Object CRUD

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/domains` | List all Domains |
| `GET` | `/api/domains/{name}` | Get a Domain by name |
| `POST` | `/api/domains` | Create a new Domain |
| `GET` | `/api/data-elements` | List all Data Elements |
| `GET` | `/api/data-elements/{name}` | Get a Data Element by name |
| `POST` | `/api/data-elements` | Create a new Data Element |
| `GET` | `/api/tables` | List all Tables |
| `GET` | `/api/tables/{name}` | Get a Table by name |
| `POST` | `/api/tables` | Create a new Table |
| `GET` | `/api/structures` | List all Structures |
| `GET` | `/api/structures/{name}` | Get a Structure by name |
| `POST` | `/api/structures` | Create a new Structure |
| `GET` | `/api/views` | List all Views |
| `GET` | `/api/views/{name}` | Get a View by name |
| `POST` | `/api/views` | Create a new View |
| `GET` | `/api/search-helps` | List all Search Helps |
| `GET` | `/api/search-helps/{name}` | Get a Search Help by name |
| `POST` | `/api/search-helps` | Create a new Search Help |
| `GET` | `/api/lock-objects` | List all Lock Objects |
| `GET` | `/api/lock-objects/{name}` | Get a Lock Object by name |
| `POST` | `/api/lock-objects` | Create a new Lock Object |

### Where-Used Analysis

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/where-used/domains/{name}` | Find all objects using a Domain |
| `GET` | `/api/where-used/data-elements/{name}` | Find all Tables/Structures using a Data Element |
| `GET` | `/api/where-used/tables/{name}` | Find all Views/SearchHelps/LockObjects using a Table |

### DDL Generation

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/ddl/tables/{name}?dialect=POSTGRESQL` | Generate CREATE TABLE DDL |
| `GET` | `/api/ddl/views/{name}?dialect=H2` | Generate CREATE VIEW DDL |

Supported dialects: `POSTGRESQL`, `H2`, `HANA`

### Web UI

Open `http://localhost:8080` in a browser to access the Data Dictionary Browser.

---

## License

This project is provided as an example / reference implementation.