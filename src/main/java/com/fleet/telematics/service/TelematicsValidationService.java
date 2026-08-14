package com.fleet.telematics.service;

import com.fleet.telematics.domain.entity.Vehicle;
import com.fleet.telematics.dto.request.TelematicsIngestRequest;
import com.fleet.telematics.exception.DataQualityValidationException;
import com.fleet.telematics.exception.OwnershipValidationException;
import com.fleet.telematics.exception.TenantNotFoundException;
import com.fleet.telematics.exception.VehicleNotFoundException;
import com.fleet.telematics.repository.TenantRepository;
import com.fleet.telematics.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class TelematicsValidationService {

    private static final Logger log = LoggerFactory.getLogger(TelematicsValidationService.class);

    // Allow up to 5 seconds clock drift skew tolerance for IoT edge sensors
    private static final Duration FUTURE_TIMESTAMP_TOLERANCE = Duration.ofSeconds(5);

    private final TenantRepository tenantRepository;
    private final VehicleRepository vehicleRepository;
    private final Clock clock;

    public TelematicsValidationService(TenantRepository tenantRepository,
                                       VehicleRepository vehicleRepository,
                                       Clock clock) {
        this.tenantRepository = tenantRepository;
        this.vehicleRepository = vehicleRepository;
        this.clock = clock;
    }

    /**
     * Executes complete validation pipeline for incoming IoT telemetry update:
     * 1. Data Quality Checks (timestamp bounds, required fields)
     * 2. Tenant Existence Check
     * 3. Vehicle Existence & Tenant Ownership Check
     *
     * @param request Telemetry ingestion request
     * @return Validated Vehicle entity
     */
    public Vehicle validate(TelematicsIngestRequest request) {
        // 1. Data Quality Validation: Future Timestamp check
        Instant now = Instant.now(clock);
        Instant maxAllowedFutureTime = now.plus(FUTURE_TIMESTAMP_TOLERANCE);

        if (request.getTimestamp() == null) {
            throw new DataQualityValidationException("Telemetry timestamp cannot be null");
        }

        if (request.getTimestamp().isAfter(maxAllowedFutureTime)) {
            log.warn("Rejected event {}: Timestamp {} is set in the future relative to server time {}",
                    request.getEventId(), request.getTimestamp(), now);
            throw new DataQualityValidationException(String.format(
                    "Timestamp '%s' is set in the future (Current server time: '%s')",
                    request.getTimestamp(), now
            ));
        }

        // 2. Tenant Existence Check
        if (!tenantRepository.existsById(request.getTenantId())) {
            log.warn("Rejected event {}: Tenant account '{}' does not exist", request.getEventId(), request.getTenantId());
            throw new TenantNotFoundException(request.getTenantId());
        }

        // 3. Vehicle Existence & Ownership Check
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> {
                    log.warn("Rejected event {}: Vehicle '{}' not found in system registry", request.getEventId(), request.getVehicleId());
                    return new VehicleNotFoundException(request.getVehicleId());
                });

        if (!vehicle.getTenant().getId().equals(request.getTenantId())) {
            log.warn("SECURITY VIOLATION / OWNERSHIP MISMATCH: Tenant '{}' attempted to submit telemetry for Vehicle '{}' owned by Tenant '{}'",
                    request.getTenantId(), request.getVehicleId(), vehicle.getTenant().getId());
            throw new OwnershipValidationException(request.getVehicleId(), request.getTenantId());
        }

        log.debug("Validation succeeded for event {} (Vehicle: {}, Tenant: {})",
                request.getEventId(), request.getVehicleId(), request.getTenantId());

        return vehicle;
    }
}
