package com.fleet.telematics.integration;

import com.fleet.telematics.domain.entity.TelematicsEvent;
import com.fleet.telematics.domain.model.IngestStatus;
import com.fleet.telematics.domain.model.IngestionResult;
import com.fleet.telematics.domain.model.TelematicsPayload;
import com.fleet.telematics.dto.request.TelematicsIngestRequest;
import com.fleet.telematics.exception.DataQualityValidationException;
import com.fleet.telematics.exception.OwnershipValidationException;
import com.fleet.telematics.repository.TelematicsEventRepository;
import com.fleet.telematics.service.TelematicsIngestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TelematicsIngestionIntegrationTest {

    @Autowired
    private TelematicsIngestionService ingestionService;

    @Autowired
    private TelematicsEventRepository eventRepository;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration Test: Full telemetry ingestion pipeline with database persistence")
    void fullIngestionPipeline_Success() {
        String eventId = "evt-integration-001";
        String vehicleId = "VEH-LOGIX-101"; // Pre-populated in Flyway V2 seed
        String tenantId = "TENANT-LOGIX-001"; // Pre-populated in Flyway V2 seed
        Instant timestamp = Instant.now().minusSeconds(10);

        TelematicsPayload payload = new TelematicsPayload(82.4, 91.5, 40.7128, -74.0060, "RUNNING", 2200, 15400.0);
        TelematicsIngestRequest request = new TelematicsIngestRequest(eventId, vehicleId, tenantId, timestamp, payload);

        // 1. Process Event
        IngestionResult result = ingestionService.processEvent(request);
        assertEquals(IngestStatus.PROCESSED, result.getStatus());

        // 2. Verify Database Persistence
        Optional<TelematicsEvent> persistedOpt = eventRepository.findByEventId(eventId);
        assertTrue(persistedOpt.isPresent());
        TelematicsEvent persisted = persistedOpt.get();

        assertEquals(eventId, persisted.getEventId());
        assertEquals(vehicleId, persisted.getVehicleId());
        assertEquals(tenantId, persisted.getTenantId());
        assertEquals(82.4, persisted.getSpeed());
        assertEquals(91.5, persisted.getFuelLevel());

        // 3. Re-ingest exact same eventId (Deduplication Check)
        IngestionResult dupResult = ingestionService.processEvent(request);
        assertEquals(IngestStatus.DUPLICATE_IGNORED, dupResult.getStatus());

        // 4. Verify DB count has not increased
        long count = eventRepository.count();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Integration Test: Reject telemetry submission when ownership check fails")
    void ownershipCheck_IntegrationFailure() {
        String eventId = "evt-illegal-002";
        String vehicleId = "VEH-LOGIX-101"; // Owned by TENANT-LOGIX-001
        String illegalTenantId = "TENANT-SWIFT-002"; // Submitting tenant is SWIFT
        Instant timestamp = Instant.now().minusSeconds(10);

        TelematicsPayload payload = new TelematicsPayload(50.0, 50.0, null, null, null, null, null);
        TelematicsIngestRequest request = new TelematicsIngestRequest(eventId, vehicleId, illegalTenantId, timestamp, payload);

        // Verify exception thrown and nothing persisted
        assertThrows(OwnershipValidationException.class, () -> ingestionService.processEvent(request));
        assertFalse(eventRepository.existsByEventId(eventId));
    }

    @Test
    @DisplayName("Integration Test: Reject telemetry event with future timestamp")
    void futureTimestamp_IntegrationFailure() {
        String eventId = "evt-future-003";
        String vehicleId = "VEH-LOGIX-101";
        String tenantId = "TENANT-LOGIX-001";
        Instant futureTimestamp = Instant.now().plusSeconds(600); // 10 minutes in future

        TelematicsPayload payload = new TelematicsPayload(50.0, 50.0, null, null, null, null, null);
        TelematicsIngestRequest request = new TelematicsIngestRequest(eventId, vehicleId, tenantId, futureTimestamp, payload);

        assertThrows(DataQualityValidationException.class, () -> ingestionService.processEvent(request));
        assertFalse(eventRepository.existsByEventId(eventId));
    }
}
