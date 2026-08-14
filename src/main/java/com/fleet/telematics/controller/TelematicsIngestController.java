package com.fleet.telematics.controller;

import com.fleet.telematics.domain.model.IngestStatus;
import com.fleet.telematics.domain.model.IngestionResult;
import com.fleet.telematics.dto.request.BatchIngestRequest;
import com.fleet.telematics.dto.request.TelematicsIngestRequest;
import com.fleet.telematics.dto.response.BatchIngestResponse;
import com.fleet.telematics.dto.response.ErrorResponse;
import com.fleet.telematics.dto.response.TelematicsIngestResponse;
import com.fleet.telematics.service.TelematicsIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/telemetry")
@Tag(name = "Telemetry Ingestion", description = "IoT telemetry data stream ingestion, validation, and deduplication API")
public class TelematicsIngestController {

    private final TelematicsIngestionService ingestionService;

    public TelematicsIngestController(TelematicsIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    @Operation(summary = "Ingest Telemetry Event", description = "Accepts single IoT telemetry device update, validates data quality & vehicle ownership, deduplicates, and saves record.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Telemetry event processed and saved successfully",
                    content = @Content(schema = @Schema(implementation = TelematicsIngestResponse.class))),
            @ApiResponse(responseCode = "200", description = "Duplicate event received and safely ignored",
                    content = @Content(schema = @Schema(implementation = TelematicsIngestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or data quality validation failed (e.g. future timestamp)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Ownership validation check failed (vehicle does not belong to specified tenant)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tenant or Vehicle not found in baseline registry",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TelematicsIngestResponse> ingestTelemetry(@Valid @RequestBody TelematicsIngestRequest request) {
        IngestionResult result = ingestionService.processEvent(request);

        TelematicsIngestResponse response = new TelematicsIngestResponse(
                result.getStatus(),
                result.getEventId(),
                request.getVehicleId(),
                request.getTenantId(),
                result.getMessage(),
                Instant.now()
        );

        if (result.getStatus() == IngestStatus.PROCESSED) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            // Duplicate event ignored gracefully without crashing or duplicate entry
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @PostMapping("/batch")
    @Operation(summary = "Batch Ingest Telemetry Stream", description = "Ingest a batch stream of IoT telemetry events with itemized result reporting.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Batch ingestion processed successfully",
                    content = @Content(schema = @Schema(implementation = BatchIngestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid batch request format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BatchIngestResponse> ingestBatch(@Valid @RequestBody BatchIngestRequest batchRequest) {
        BatchIngestResponse response = ingestionService.processBatch(batchRequest);
        return ResponseEntity.ok(response);
    }
}
