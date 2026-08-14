package com.fleet.telematics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Authorized vehicle status and ownership details")
public class VehicleStatusResponse {

    private String vehicleId;
    private String tenantId;
    private String tenantName;
    private String vin;
    private String model;
    private String status;
    private Double lastKnownSpeed;
    private Double lastKnownFuelLevel;
    private Instant lastSeenTimestamp;

    public VehicleStatusResponse() {
    }

    public VehicleStatusResponse(String vehicleId, String tenantId, String tenantName, String vin, String model, String status) {
        this.vehicleId = vehicleId;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.vin = vin;
        this.model = model;
        this.status = status;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getLastKnownSpeed() {
        return lastKnownSpeed;
    }

    public void setLastKnownSpeed(Double lastKnownSpeed) {
        this.lastKnownSpeed = lastKnownSpeed;
    }

    public Double getLastKnownFuelLevel() {
        return lastKnownFuelLevel;
    }

    public void setLastKnownFuelLevel(Double lastKnownFuelLevel) {
        this.lastKnownFuelLevel = lastKnownFuelLevel;
    }

    public Instant getLastSeenTimestamp() {
        return lastSeenTimestamp;
    }

    public void setLastSeenTimestamp(Instant lastSeenTimestamp) {
        this.lastSeenTimestamp = lastSeenTimestamp;
    }
}
