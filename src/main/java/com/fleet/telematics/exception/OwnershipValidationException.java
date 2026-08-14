package com.fleet.telematics.exception;

public class OwnershipValidationException extends RuntimeException {
    private final String vehicleId;
    private final String submittingTenantId;

    public OwnershipValidationException(String vehicleId, String submittingTenantId) {
        super(String.format("Ownership validation check failed: Vehicle '%s' does not belong to submitting tenant '%s'", vehicleId, submittingTenantId));
        this.vehicleId = vehicleId;
        this.submittingTenantId = submittingTenantId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getSubmittingTenantId() {
        return submittingTenantId;
    }
}
