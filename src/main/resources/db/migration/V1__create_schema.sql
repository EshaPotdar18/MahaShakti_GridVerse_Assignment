-- V1__create_schema.sql
-- Fleet Telematics Schema Definition

CREATE TABLE tenants (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE vehicles (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    vin VARCHAR(64) UNIQUE NOT NULL,
    model VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vehicles_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE TABLE telematics_events (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL UNIQUE,
    vehicle_id VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    event_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    speed DOUBLE PRECISION NOT NULL,
    fuel_level DOUBLE PRECISION NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    engine_status VARCHAR(32),
    rpm INTEGER,
    odometer DOUBLE PRECISION,
    raw_payload_json TEXT,
    CONSTRAINT fk_events_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
    CONSTRAINT fk_events_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

-- Indexing for performance and lookup speed
CREATE INDEX idx_telematics_events_event_id ON telematics_events(event_id);
CREATE INDEX idx_telematics_events_vehicle_timestamp ON telematics_events(vehicle_id, event_timestamp DESC);
CREATE INDEX idx_telematics_events_tenant_timestamp ON telematics_events(tenant_id, event_timestamp DESC);
CREATE INDEX idx_vehicles_tenant_id ON vehicles(tenant_id);
