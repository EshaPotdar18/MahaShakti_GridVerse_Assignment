-- V2__seed_initial_data.sql
-- Seed baseline data for Tenants and Authorized Vehicles

-- 1. Insert Tenants
INSERT INTO tenants (id, name, contact_email, status, created_at) VALUES
('TENANT-LOGIX-001', 'LogiX Logistics Corp', 'ops@logix-logistics.com', 'ACTIVE', CURRENT_TIMESTAMP),
('TENANT-SWIFT-002', 'Swift Express Fleet', 'fleet@swiftexpress.com', 'ACTIVE', CURRENT_TIMESTAMP);

-- 2. Insert Authorized Vehicles assigned to Tenants
-- Vehicles for Tenant 1 (LogiX Logistics Corp)
INSERT INTO vehicles (id, tenant_id, vin, model, status, created_at) VALUES
('VEH-LOGIX-101', 'TENANT-LOGIX-001', '1HGCR2F83HA000101', 'Volvo FH16 Semi-Truck', 'ACTIVE', CURRENT_TIMESTAMP),
('VEH-LOGIX-102', 'TENANT-LOGIX-001', '1HGCR2F83HA000102', 'Scania R500 Heavy Hauler', 'ACTIVE', CURRENT_TIMESTAMP),
('VEH-LOGIX-103', 'TENANT-LOGIX-001', '1HGCR2F83HA000103', 'Freightliner Cascadia EV', 'ACTIVE', CURRENT_TIMESTAMP);

-- Vehicles for Tenant 2 (Swift Express Fleet)
INSERT INTO vehicles (id, tenant_id, vin, model, status, created_at) VALUES
('VEH-SWIFT-201', 'TENANT-SWIFT-002', '2C3CDZFJ9KH000201', 'Mercedes-Benz Actros 1845', 'ACTIVE', CURRENT_TIMESTAMP),
('VEH-SWIFT-202', 'TENANT-SWIFT-002', '2C3CDZFJ9KH000202', 'DAF XF 530 Super Space', 'ACTIVE', CURRENT_TIMESTAMP);
