package com.fleet.telematics.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Batch telemetry ingestion request payload containing multiple IoT events")
public class BatchIngestRequest {

    @Schema(description = "List of telemetry events to ingest in batch")
    @NotEmpty(message = "events list cannot be empty")
    @Valid
    private List<TelematicsIngestRequest> events;

    public BatchIngestRequest() {
    }

    public BatchIngestRequest(List<TelematicsIngestRequest> events) {
        this.events = events;
    }

    public List<TelematicsIngestRequest> getEvents() {
        return events;
    }

    public void setEvents(List<TelematicsIngestRequest> events) {
        this.events = events;
    }
}
