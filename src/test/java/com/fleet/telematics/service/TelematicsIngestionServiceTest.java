package com.fleet.telematics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet.telematics.domain.entity.Tenant;
import com.fleet.telematics.domain.entity.TelematicsEvent;
import com.fleet.telematics.domain.entity.Vehicle;
import com.fleet.telematics.domain.model.IngestStatus;
import com.fleet.telematics.domain.model.IngestionResult;
import com.fleet.telematics.domain.model.TelematicsPayload;
import com.fleet.telematics.dto.request.BatchIngestRequest;
import com.fleet.telematics.dto.request.TelematicsIngestRequest;
import com.fleet.telematics.dto.response.BatchIngestResponse;
import com.fleet.telematics.repository.TelematicsEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelematicsIngestionServiceTest {

    @Mock
    private TelematicsValidationService validationService;

    @Mock
    private TelematicsEventRepository eventRepository;

    private TelematicsIngestionService ingestionService;
    private final Instant now = Instant.parse("2026-08-13T12:00:00Z");

    private Tenant tenant;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(now, ZoneId.of("UTC"));
        ingestionService = new TelematicsIngestionService(validationService, eventRepository, new ObjectMapper(), fixedClock);

        tenant = new Tenant("TENANT-LOGIX-001", "LogiX Corp", "ops@logix.com", "ACTIVE");
        vehicle = new Vehicle("VEH-LOGIX-101", tenant, "VIN101", "Volvo FH16", "ACTIVE");
    }

    @Test
    @DisplayName("Should process and persist new unique telemetry event successfully")
    void processEvent_Success() {
        TelematicsPayload payload = new TelematicsPayload(70.0, 85.0, 37.77, -122.41, "RUNNING", 2100, 45000.0);
        TelematicsIngestRequest request = new TelematicsIngestRequest("evt-unique-100", "VEH-LOGIX-101", "TENANT-LOGIX-001", now, payload);

        when(eventRepository.existsByEventId("evt-unique-100")).thenReturn(false);
        when(validationService.validate(request)).thenReturn(vehicle);

        TelematicsEvent savedEvent = new TelematicsEvent("evt-unique-100", "VEH-LOGIX-101", "TENANT-LOGIX-001", now,
                70.0, 85.0, 37.77, -122.41, "RUNNING", 2100, 45000.0, "{}");
        when(eventRepository.save(any(TelematicsEvent.class))).thenReturn(savedEvent);

        IngestionResult result = ingestionService.processEvent(request);

        assertEquals(IngestStatus.PROCESSED, result.getStatus());
        assertEquals("evt-unique-100", result.getEventId());
        assertNotNull(result.getSavedEvent());

        ArgumentCaptor<TelematicsEvent> eventCaptor = ArgumentCaptor.forClass(TelematicsEvent.class);
        verify(eventRepository).save(eventCaptor.capture());
        assertEquals("evt-unique-100", eventCaptor.getValue().getEventId());
        assertEquals("VEH-LOGIX-101", eventCaptor.getValue().getVehicleId());
    }

    @Test
    @DisplayName("Should handle duplicate eventId gracefully without re-validating or throwing exception")
    void processEvent_DuplicateEventId_GracefullyIgnored() {
        TelematicsPayload payload = new TelematicsPayload(70.0, 85.0, null, null, null, null, null);
        TelematicsIngestRequest request = new TelematicsIngestRequest("evt-dup-999", "VEH-LOGIX-101", "TENANT-LOGIX-001", now, payload);

        when(eventRepository.existsByEventId("evt-dup-999")).thenReturn(true);

        IngestionResult result = ingestionService.processEvent(request);

        assertEquals(IngestStatus.DUPLICATE_IGNORED, result.getStatus());
        assertEquals("evt-dup-999", result.getEventId());
        assertNull(result.getSavedEvent());

        verifyNoInteractions(validationService);
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should process batch telemetry stream and return aggregate totals")
    void processBatch_Success() {
        TelematicsPayload payload = new TelematicsPayload(70.0, 85.0, null, null, null, null, null);
        TelematicsIngestRequest req1 = new TelematicsIngestRequest("evt-batch-1", "VEH-LOGIX-101", "TENANT-LOGIX-001", now, payload);
        TelematicsIngestRequest req2 = new TelematicsIngestRequest("evt-batch-2-dup", "VEH-LOGIX-101", "TENANT-LOGIX-001", now, payload);

        when(eventRepository.existsByEventId("evt-batch-1")).thenReturn(false);
        when(eventRepository.existsByEventId("evt-batch-2-dup")).thenReturn(true);
        when(validationService.validate(req1)).thenReturn(vehicle);

        TelematicsEvent savedEvent1 = new TelematicsEvent("evt-batch-1", "VEH-LOGIX-101", "TENANT-LOGIX-001", now, 70.0, 85.0, null, null, null, null, null, "{}");
        when(eventRepository.save(any())).thenReturn(savedEvent1);

        BatchIngestRequest batchRequest = new BatchIngestRequest(List.of(req1, req2));
        BatchIngestResponse batchResponse = ingestionService.processBatch(batchRequest);

        assertEquals(2, batchResponse.getTotalReceived());
        assertEquals(1, batchResponse.getTotalProcessed());
        assertEquals(1, batchResponse.getTotalDuplicatesSkipped());
        assertEquals(0, batchResponse.getTotalRejected());
        assertEquals(2, batchResponse.getResults().size());
    }
}
