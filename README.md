# 🚚 Fleet Telematics Stream & Validation Engine

A lightweight, high-performance, and robust backend service built with **Java 21**, **Spring Boot 3.3.x**, and **PostgreSQL** for real-time IoT vehicle fleet telemetry ingestion, ownership verification, data quality validation, and stream deduplication.

---

## 🌟 Architecture & Design Highlights

### 1. Data Initialization & Setup
- **Pre-populated Baseline Registry**: Seeded with 2 distinct tenant platform accounts (`TENANT-LOGIX-001` and `TENANT-SWIFT-002`) and 5 authorized delivery vehicles explicitly assigned to their owning tenants via Flyway database migrations (`V1__create_schema.sql`, `V2__seed_initial_data.sql`) and Spring `CommandLineRunner` initialization.
- **Multitenancy Model**: Enforces strict logical isolation between tenant platform accounts and their associated vehicle fleets.

### 2. Validation & Processing Pipeline
Every incoming telemetry data update (`POST /api/v1/telemetry`) flows through a multi-stage validation pipeline:
1. **Jakarta Bean Validation**: Ensures required fields (`eventId`, `vehicleId`, `tenantId`, `timestamp`, `payload`) are present and syntactically valid.
2. **Ownership Validation Check**: Verifies that the `vehicleId` belongs to the `tenantId` submitting the payload. Rejects unauthorized or cross-tenant submissions immediately with **HTTP 403 Forbidden** (`OWNERSHIP_MISMATCH`).
3. **Data Quality Validation**: Rejects records if the timestamp is set in the future relative to server time (with a 5-second clock skew tolerance for IoT edge sensors) or if telemetry metrics (speed, fuel level) fall outside acceptable physical bounds. Returns **HTTP 400 Bad Request**.
4. **Thread-Safe Data Deduplication**: Employs a dual-layer deduplication system combining a `ConcurrentHashMap` in-memory stream cache with a database unique index on `event_id`. Duplicate events are skipped gracefully with **HTTP 200 OK** (`DUPLICATE_IGNORED`) without crashing or creating duplicate DB records.

---

## 🛠️ Technology Stack

| Category | Technology |
|---|---|
| **Language** | Java 21 / 17 |
| **Framework** | Spring Boot 3.3.2 |
| **Build Tool** | Maven 3.9.6 (with Maven Wrapper `mvnw`) |
| **Web & Validation** | Spring Web MVC, Jakarta Bean Validation |
| **Persistence** | Spring Data JPA, Hibernate, PostgreSQL / H2 |
| **Database Migrations** | Flyway |
| **Testing** | JUnit 5, Mockito, MockMvc |
| **API Documentation** | OpenAPI 3 / Swagger UI (Springdoc) |
| **Containerization** | Docker & Docker Compose |

---

## 🚀 Quick Start Guide

### Prerequisites
- Java JDK 17 or Java 21 installed.
- Docker & Docker Compose (optional, for production container execution).

---

### Running the Application

#### Option A: Running Locally with Embedded Database (Dev Profile)
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```
The application will start on port `8080` with an in-memory PostgreSQL-compatible H2 database populated via Flyway.

#### Option B: Running with Docker Compose & PostgreSQL (Prod Profile)
```bash
docker-compose up --build
```
This launches a PostgreSQL 15 database container alongside the Spring Boot backend container.

---

## 🧪 Running Automated Tests

Run the complete automated test suite (19 unit, integration, and concurrency tests):

```bash
# Run all tests using Maven
mvnw.cmd test
```

### Test Coverage Highlights
- **`TelematicsValidationServiceTest`**: Verifies vehicle ownership mismatch rejection, future timestamp rejection, and missing tenant/vehicle errors.
- **`TelematicsIngestionServiceTest`**: Verifies stream ingestion, deduplication skipping, and batch processing logic.
- **`TelematicsIngestControllerTest`**: MockMvc API endpoint tests validating HTTP status codes (201 Created, 200 OK, 403 Forbidden, 400 Bad Request).
- **`TelematicsIngestionIntegrationTest`**: End-to-end integration flow against database schema.
- **`ConcurrencyDeduplicationIntegrationTest`**: Multi-threaded race condition test running 10 concurrent requests simultaneously with the same `eventId` to verify thread safety.

---

## 📡 API Endpoints & Usage

### 1. Ingest Single Telemetry Event
**`POST /api/v1/telemetry`**

#### Request Payload
```json
{
  "eventId": "evt-8f92a10b-3c4d-4e5f-b6a7-890123456789",
  "vehicleId": "VEH-LOGIX-101",
  "tenantId": "TENANT-LOGIX-001",
  "timestamp": "2026-08-13T12:00:00Z",
  "payload": {
    "speed": 65.5,
    "fuelLevel": 78.2,
    "latitude": 37.7749,
    "longitude": -122.4194,
    "engineStatus": "RUNNING",
    "rpm": 2100,
    "odometer": 45210.5
  }
}
```

#### Response (201 Created)
```json
{
  "status": "PROCESSED",
  "eventId": "evt-8f92a10b-3c4d-4e5f-b6a7-890123456789",
  "vehicleId": "VEH-LOGIX-101",
  "tenantId": "TENANT-LOGIX-001",
  "message": "Telemetry event ingested successfully",
  "processedAt": "2026-08-13T12:00:01Z"
}
```

---

### 2. Ingest Batch Telemetry Stream
**`POST /api/v1/telemetry/batch`**

#### Request Payload
```json
{
  "events": [
    {
      "eventId": "evt-batch-001",
      "vehicleId": "VEH-LOGIX-101",
      "tenantId": "TENANT-LOGIX-001",
      "timestamp": "2026-08-13T12:00:00Z",
      "payload": { "speed": 70.0, "fuelLevel": 85.0 }
    },
    {
      "eventId": "evt-batch-002",
      "vehicleId": "VEH-SWIFT-201",
      "tenantId": "TENANT-SWIFT-002",
      "timestamp": "2026-08-13T12:00:00Z",
      "payload": { "speed": 55.0, "fuelLevel": 90.0 }
    }
  ]
}
```

---

### 3. Query Tenants & Baseline Registry
- **List All Tenants**: `GET /api/v1/tenants`
- **Get Tenant Vehicles**: `GET /api/v1/tenants/TENANT-LOGIX-001/vehicles`
- **Get Vehicle Status & Latest Telemetry Snapshot**: `GET /api/v1/vehicles/VEH-LOGIX-101`
- **Query Telemetry History (Paginated)**: `GET /api/v1/telemetry/events?tenantId=TENANT-LOGIX-001&vehicleId=VEH-LOGIX-101`

---

## 📄 Interactive API Documentation (Swagger / OpenAPI)

Once the application is running, view the interactive OpenAPI UI at:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## ⚡ Quick Demo Script (PowerShell)

Run the included interactive stream demo script:
```powershell
powershell -ExecutionPolicy Bypass -File .\demo_stream.ps1
```
This script tests system health, fetches baseline data, streams valid telemetry, attempts duplicate event submission, tests cross-tenant ownership rejection, and verifies future timestamp rejection.
