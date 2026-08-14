package com.fleet.telematics.integration;

import com.fleet.telematics.domain.model.IngestStatus;
import com.fleet.telematics.domain.model.IngestionResult;
import com.fleet.telematics.domain.model.TelematicsPayload;
import com.fleet.telematics.dto.request.TelematicsIngestRequest;
import com.fleet.telematics.repository.TelematicsEventRepository;
import com.fleet.telematics.service.TelematicsIngestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class ConcurrencyDeduplicationIntegrationTest {

    @Autowired
    private TelematicsIngestionService ingestionService;

    @Autowired
    private TelematicsEventRepository eventRepository;

    @Test
    @DisplayName("Concurrency Test: 10 concurrent threads streaming the exact same eventId should resulting in exactly 1 saved record and 9 duplicates")
    void concurrentStreamDeduplication_ThreadSafety() throws Exception {
        String concurrentEventId = "evt-concurrent-race-777";
        String vehicleId = "VEH-LOGIX-101";
        String tenantId = "TENANT-LOGIX-001";
        Instant timestamp = Instant.now().minusSeconds(5);

        TelematicsPayload payload = new TelematicsPayload(60.0, 70.0, null, null, null, null, null);
        TelematicsIngestRequest request = new TelematicsIngestRequest(concurrentEventId, vehicleId, tenantId, timestamp, payload);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<IngestionResult>> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> ingestionService.processEvent(request));
        }

        List<Future<IngestionResult>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        int processedCount = 0;
        int duplicateCount = 0;

        for (Future<IngestionResult> future : futures) {
            IngestionResult result = future.get();
            if (result.getStatus() == IngestStatus.PROCESSED) {
                processedCount++;
            } else if (result.getStatus() == IngestStatus.DUPLICATE_IGNORED) {
                duplicateCount++;
            }
        }

        assertEquals(1, processedCount, "Exactly 1 concurrent request must be processed and saved");
        assertEquals(9, duplicateCount, "Exactly 9 duplicate concurrent requests must be ignored gracefully");

        // Verify database contains exactly 1 record with this eventId
        assertEquals(1, eventRepository.findAll().stream().filter(e -> concurrentEventId.equals(e.getEventId())).count());
    }
}
