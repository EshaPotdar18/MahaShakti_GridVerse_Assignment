package com.fleet.telematics.exception;

public class TenantNotFoundException extends RuntimeException {
    public TenantNotFoundException(String tenantId) {
        super("Tenant account not found: " + tenantId);
    }
}
