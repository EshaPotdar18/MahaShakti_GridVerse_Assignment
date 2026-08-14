package com.fleet.telematics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet.telematics.domain.entity.TelematicsEvent;
import com.fleet.telematics.domain.model.IngestionResult;
import com.fleet.telematics.domain.model.TelematicsPayload;
import com.fleet.telematics.dto.request.TelematicsIngestRequest;
import com.fleet.telematics.exception.DataQualityValidationException;
import com.fleet.telematics.exception.GlobalExceptionHandler;
import com.fleet.telematics.exception.OwnershipValidationException;
import com.fleet.telematics.service.TelematicsIngestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {TelematicsIngestController.class, GlobalExceptionHandler.class})
class TelematicsIngestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TelematicsIngestionService ingestionService;

    private TelematicsIngestRequest validRequest;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.parse("2026-08-13T12:00:00Z");
        TelematicsPayload payload = new TelematicsPayload(65.0, 75.0, 37.77, -122.41, "RUNNING", 1800, 12000.0);
        validRequest = new TelematicsIngestRequest("evt-api-101", "VEH-LOGIX-101", "TENANT-LOGIX-001", now, payload);
    }

    @Test
    @DisplayName("POST /api/v1/telemetry - Should return 201 CREATED when ingestion succeeds")
    void ingestTelemetry_Success() throws Exception {
        TelematicsEvent savedEvent = new TelematicsEvent("evt-api-101", "VEH-LOGIX-101", "TENANT-LOGIX-001", now, 65.0, 75.0, 37.77, -122.41, "RUNNING", 1800, 12000.0, "{}");
        when(ingestionService.processEvent(any())).thenReturn(IngestionResult.processed(savedEvent));

        mockMvc.perform(post("/api/v1/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PROCESSED")))
                .andExpect(jsonPath("$.eventId", is("evt-api-101")))
                .andExpect(jsonPath("$.vehicleId", is("VEH-LOGIX-101")))
                .andExpect(jsonPath("$.tenantId", is("TENANT-LOGIX-001")));
    }

    @Test
    @DisplayName("POST /api/v1/telemetry - Should return 200 OK when duplicate eventId is safely skipped")
    void ingestTelemetry_Duplicate_Returns200OK() throws Exception {
        when(ingestionService.processEvent(any())).thenReturn(IngestionResult.duplicate("evt-api-101"));

        mockMvc.perform(post("/api/v1/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DUPLICATE_IGNORED")))
                .andExpect(jsonPath("$.eventId", is("evt-api-101")))
                .andExpect(jsonPath("$.message", containsString("Duplicate event detected")));
    }

    @Test
    @DisplayName("POST /api/v1/telemetry - Should return 403 FORBIDDEN on ownership mismatch check failure")
    void ingestTelemetry_OwnershipMismatch_Returns403Forbidden() throws Exception {
        when(ingestionService.processEvent(any())).thenThrow(new OwnershipValidationException("VEH-LOGIX-101", "TENANT-SWIFT-002"));

        mockMvc.perform(post("/api/v1/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode", is("OWNERSHIP_MISMATCH")))
                .andExpect(jsonPath("$.message", containsString("does not belong to submitting tenant")));
    }

    @Test
    @DisplayName("POST /api/v1/telemetry - Should return 400 BAD REQUEST on future timestamp data quality failure")
    void ingestTelemetry_FutureTimestamp_Returns400BadRequest() throws Exception {
        when(ingestionService.processEvent(any())).thenThrow(new DataQualityValidationException("Timestamp is set in the future"));

        mockMvc.perform(post("/api/v1/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("DATA_QUALITY_INVALID")))
                .andExpect(jsonPath("$.message", containsString("in the future")));
    }

    @Test
    @DisplayName("POST /api/v1/telemetry - Should return 400 BAD REQUEST when mandatory fields are missing")
    void ingestTelemetry_MissingFields_Returns400BadRequest() throws Exception {
        TelematicsIngestRequest invalidReq = new TelematicsIngestRequest();
        invalidReq.setEventId(""); // Blank eventId
        invalidReq.setVehicleId(null); // Missing vehicleId

        mockMvc.perform(post("/api/v1/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.fieldErrors.eventId", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.vehicleId", notNullValue()));
    }
}
