package com.fleet.telematics.dto.request;

import com.fleet.telematics.domain.model.TelematicsPayload;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Schema(description = "Telemetry ingestion event payload streamed from vehicle IoT devices")
public class TelematicsIngestRequest {

    @Schema(description = "Unique identifier for the telemetry event", example = "evt-8f92a10b-3c4d-4e5f-b6a7-890123456789")
    @NotBlank(message = "eventId is required and cannot be blank")
    private String eventId;

    @Schema(description = "Unique identifier of the tracking vehicle", example = "VEH-LOGIX-101")
    @NotBlank(message = "vehicleId is required and cannot be blank")
    private String vehicleId;

    @Schema(description = "Unique identifier of the submitting tenant company", example = "TENANT-LOGIX-001")
    @NotBlank(message = "tenantId is required and cannot be blank")
    private String tenantId;

    @Schema(description = "Timestamp when the telemetry data was captured on device (ISO-8601 UTC)", example = "2026-08-13T12:00:00Z")
    @NotNull(message = "timestamp is required and cannot be null")
    private Instant timestamp;

    @Schema(description = "Telemetry telemetry metrics payload (speed, fuel level, etc.)")
    @NotNull(message = "payload is required and cannot be null")
    @Valid
    private TelematicsPayload payload;

    public TelematicsIngestRequest() {
    }

    public TelematicsIngestRequest(String eventId, String vehicleId, String tenantId, Instant timestamp, TelematicsPayload payload) {
        this.eventId = eventId;
        this.vehicleId = vehicleId;
        this.tenantId = tenantId;
        this.timestamp = timestamp;
        this.payload = payload;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public TelematicsPayload getPayload() {
        return payload;
    }

    public void setPayload(TelematicsPayload payload) {
        this.payload = payload;
    }
}
