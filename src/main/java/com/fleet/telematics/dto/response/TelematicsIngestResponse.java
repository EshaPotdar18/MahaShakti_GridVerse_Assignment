package com.fleet.telematics.dto.response;

import com.fleet.telematics.domain.model.IngestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Response returned after processing a telemetry ingestion event")
public class TelematicsIngestResponse {

    @Schema(description = "Status of the ingestion processing", example = "PROCESSED")
    private IngestStatus status;

    @Schema(description = "Event ID", example = "evt-8f92a10b-3c4d-4e5f-b6a7-890123456789")
    private String eventId;

    @Schema(description = "Vehicle ID", example = "VEH-LOGIX-101")
    private String vehicleId;

    @Schema(description = "Tenant ID", example = "TENANT-LOGIX-001")
    private String tenantId;

    @Schema(description = "Human-readable summary message", example = "Telemetry event ingested successfully")
    private String message;

    @Schema(description = "Timestamp when the event was processed by the server")
    private Instant processedAt;

    public TelematicsIngestResponse() {
    }

    public TelematicsIngestResponse(IngestStatus status, String eventId, String vehicleId, String tenantId, String message, Instant processedAt) {
        this.status = status;
        this.eventId = eventId;
        this.vehicleId = vehicleId;
        this.tenantId = tenantId;
        this.message = message;
        this.processedAt = processedAt;
    }

    public IngestStatus getStatus() {
        return status;
    }

    public void setStatus(IngestStatus status) {
        this.status = status;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
