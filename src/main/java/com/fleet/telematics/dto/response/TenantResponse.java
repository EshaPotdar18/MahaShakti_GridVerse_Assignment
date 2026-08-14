package com.fleet.telematics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Tenant company details and assigned fleet vehicles")
public class TenantResponse {

    private String tenantId;
    private String name;
    private String contactEmail;
    private String status;
    private List<VehicleStatusResponse> vehicles;

    public TenantResponse() {
    }

    public TenantResponse(String tenantId, String name, String contactEmail, String status, List<VehicleStatusResponse> vehicles) {
        this.tenantId = tenantId;
        this.name = name;
        this.contactEmail = contactEmail;
        this.status = status;
        this.vehicles = vehicles;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<VehicleStatusResponse> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<VehicleStatusResponse> vehicles) {
        this.vehicles = vehicles;
    }
}
