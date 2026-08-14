package com.fleet.telematics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet.telematics.domain.entity.TelematicsEvent;
import com.fleet.telematics.domain.entity.Vehicle;
import com.fleet.telematics.domain.model.IngestStatus;
import com.fleet.telematics.domain.model.IngestionResult;
import com.fleet.telematics.domain.model.TelematicsPayload;
import com.fleet.telematics.dto.request.BatchIngestRequest;
import com.fleet.telematics.dto.request.TelematicsIngestRequest;
import com.fleet.telematics.dto.response.BatchIngestResponse;
import com.fleet.telematics.dto.response.TelematicsIngestResponse;
import com.fleet.telematics.repository.TelematicsEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TelematicsIngestionService {

    private static final Logger log = LoggerFactory.getLogger(TelematicsIngestionService.class);

    private final TelematicsValidationService validationService;
    private final TelematicsEventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    // High-performance thread-safe in-memory deduplication cache for streaming IoT events
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    public TelematicsIngestionService(TelematicsValidationService validationService,
                                      TelematicsEventRepository eventRepository,
                                      ObjectMapper objectMapper,
                                      Clock clock) {
        this.validationService = validationService;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    /**
     * Processes single incoming telemetry event stream update.
     * Guaranteed thread-safe deduplication under high-concurrency streaming.
     *
     * @param request The telemetry event update
     * @return IngestionResult containing status and details
     */
    @Transactional
    public IngestionResult processEvent(TelematicsIngestRequest request) {
        String eventId = request.getEventId();

        // 1. Thread-safe atomic check & add in memory cache
        if (!processedEventIds.add(eventId)) {
            log.info("Duplicate telemetry event detected in stream cache and skipped: eventId={}", eventId);
            return IngestionResult.duplicate(eventId);
        }

        // 2. Secondary check against persistent database registry
        if (eventRepository.existsByEventId(eventId)) {
            log.info("Duplicate telemetry event detected in database and skipped: eventId={}", eventId);
            return IngestionResult.duplicate(eventId);
        }

        // 3. Execute Data Quality & Ownership Validation Checks
        Vehicle validatedVehicle = validationService.validate(request);

        // 4. Serialize payload to raw JSON string for audit storage
        String rawPayloadJson = serializePayload(request.getPayload());

        // 5. Construct TelematicsEvent entity
        TelematicsEvent event = new TelematicsEvent(
                eventId,
                validatedVehicle.getId(),
                request.getTenantId(),
                request.getTimestamp(),
                request.getPayload().getSpeed(),
                request.getPayload().getFuelLevel(),
                request.getPayload().getLatitude(),
                request.getPayload().getLongitude(),
                request.getPayload().getEngineStatus(),
                request.getPayload().getRpm(),
                request.getPayload().getOdometer(),
                rawPayloadJson
        );

        // 6. Persist to Database
        try {
            TelematicsEvent savedEvent = eventRepository.save(event);
            log.info("Successfully persisted telemetry event {}: Vehicle={}, Tenant={}, Speed={} km/h, Fuel={}%",
                    savedEvent.getEventId(), savedEvent.getVehicleId(), savedEvent.getTenantId(),
                    savedEvent.getSpeed(), savedEvent.getFuelLevel());
            return IngestionResult.processed(savedEvent);
        } catch (DataIntegrityViolationException e) {
            log.warn("Database unique constraint triggered on duplicate eventId {}. Gracefully ignoring.", eventId);
            return IngestionResult.duplicate(eventId);
        }
    }

    /**
     * Batch ingestion processing for IoT telematics stream updates.
     *
     * @param batchRequest Batch containing multiple telemetry events
     * @return BatchIngestResponse with aggregated summary and itemized results
     */
    public BatchIngestResponse processBatch(BatchIngestRequest batchRequest) {
        List<TelematicsIngestResponse> itemResults = new ArrayList<>();
        int processedCount = 0;
        int duplicatesSkippedCount = 0;
        int rejectedCount = 0;

        for (TelematicsIngestRequest singleRequest : batchRequest.getEvents()) {
            try {
                IngestionResult result = processEvent(singleRequest);
                if (result.getStatus() == IngestStatus.PROCESSED) {
                    processedCount++;
                } else if (result.getStatus() == IngestStatus.DUPLICATE_IGNORED) {
                    duplicatesSkippedCount++;
                }

                itemResults.add(new TelematicsIngestResponse(
                        result.getStatus(),
                        singleRequest.getEventId(),
                        singleRequest.getVehicleId(),
                        singleRequest.getTenantId(),
                        result.getMessage(),
                        Instant.now(clock)
                ));
            } catch (Exception ex) {
                rejectedCount++;
                log.warn("Batch event {} failed processing: {}", singleRequest.getEventId(), ex.getMessage());
                itemResults.add(new TelematicsIngestResponse(
                        IngestStatus.REJECTED,
                        singleRequest.getEventId(),
                        singleRequest.getVehicleId(),
                        singleRequest.getTenantId(),
                        "Rejected: " + ex.getMessage(),
                        Instant.now(clock)
                ));
            }
        }

        return new BatchIngestResponse(
                batchRequest.getEvents().size(),
                processedCount,
                duplicatesSkippedCount,
                rejectedCount,
                itemResults
        );
    }

    private String serializePayload(TelematicsPayload payload) {
        if (payload == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize payload to JSON", e);
            return "{}";
        }
    }
}
