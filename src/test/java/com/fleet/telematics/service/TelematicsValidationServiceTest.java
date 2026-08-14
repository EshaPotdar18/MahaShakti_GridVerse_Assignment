package com.fleet.telematics.service;

import com.fleet.telematics.domain.entity.Tenant;
import com.fleet.telematics.domain.entity.Vehicle;
import com.fleet.telematics.domain.model.TelematicsPayload;
import com.fleet.telematics.dto.request.TelematicsIngestRequest;
import com.fleet.telematics.exception.DataQualityValidationException;
import com.fleet.telematics.exception.OwnershipValidationException;
import com.fleet.telematics.exception.TenantNotFoundException;
import com.fleet.telematics.exception.VehicleNotFoundException;
import com.fleet.telematics.repository.TenantRepository;
import com.fleet.telematics.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelematicsValidationServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    private Clock fixedClock;
    private TelematicsValidationService validationService;

    private final Instant now = Instant.parse("2026-08-13T12:00:00Z");
    private Tenant tenantLogix;
    private Tenant tenantSwift;
    private Vehicle vehicleLogix;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(now, ZoneId.of("UTC"));
        validationService = new TelematicsValidationService(tenantRepository, vehicleRepository, fixedClock);

        tenantLogix = new Tenant("TENANT-LOGIX-001", "LogiX Corp", "logix@test.com", "ACTIVE");
        tenantSwift = new Tenant("TENANT-SWIFT-002", "Swift Corp", "swift@test.com", "ACTIVE");

        vehicleLogix = new Vehicle("VEH-LOGIX-101", tenantLogix, "VIN101", "Volvo FH16", "ACTIVE");
    }

    @Test
    @DisplayName("Should pass validation when vehicle belongs to tenant and timestamp is valid")
    void validate_Success() {
        TelematicsPayload payload = new TelematicsPayload(65.0, 80.0, 37.77, -122.41, "RUNNING", 2000, 50000.0);
        TelematicsIngestRequest request = new TelematicsIngestRequest("evt-001", "VEH-LOGIX-101", "TENANT-LOGIX-001", now.minusSeconds(10), payload);

        when(tenantRepository.existsById("TENANT-LOGIX-001")).thenReturn(true);
        when(vehicleRepository.findById("VEH-LOGIX-101")).thenReturn(Optional.of(vehicleLogix));

        Vehicle validatedVehicle = validationService.validate(request);

        assertNotNull(validatedVehicle);
        assertEquals("VEH-LOGIX-101", validatedVehicle.getId());
        assertEquals("TENANT-LOGIX-001", validatedVehicle.getTenant().getId());
    }

    @Test
    @DisplayName("Should fail ownership check when submitting tenant does not own the vehicle")
    void validate_OwnershipMismatch_ThrowsException() {
        TelematicsPayload payload = new TelematicsPayload(65.0, 80.0, null, null, null, null, null);
        // Tenant SWIFT attempts to submit data for Vehicle owned by Tenant LOGIX
        TelematicsIngestRequest request = new TelematicsIngestRequest("evt-002", "VEH-LOGIX-101", "TENANT-SWIFT-002", now.minusSeconds(5), payload);

        when(tenantRepository.existsById("TENANT-SWIFT-002")).thenReturn(true);
        when(vehicleRepository.findById("VEH-LOGIX-101")).thenReturn(Optional.of(vehicleLogix));

        OwnershipValidationException exception = assertThrows(OwnershipValidationException.class, () ->
                validationService.validate(request)
        );

        assertEquals("VEH-LOGIX-101", exception.getVehicleId());
        assertEquals("TENANT-SWIFT-002", exception.getSubmittingTenantId());
        assertTrue(exception.getMessage().contains("does not belong to submitting tenant"));
    }

    @Test
    @DisplayName("Should fail data quality check when timestamp is in the future")
    void validate_FutureTimestamp_ThrowsException() {
        TelematicsPayload payload = new TelematicsPayload(65.0, 80.0, null, null, null, null, null);
        // Future timestamp 1 hour into the future
        Instant futureTimestamp = now.plusSeconds(3600);
        TelematicsIngestRequest request = new TelematicsIngestRequest("evt-003", "VEH-LOGIX-101", "TENANT-LOGIX-001", futureTimestamp, payload);

        DataQualityValidationException exception = assertThrows(DataQualityValidationException.class, () ->
                validationService.validate(request)
        );

        assertTrue(exception.getMessage().contains("set in the future"));
    }

    @Test
    @DisplayName("Should fail when tenant account does not exist")
    void validate_TenantNotFound_ThrowsException() {
        TelematicsPayload payload = new TelematicsPayload(65.0, 80.0, null, null, null, null, null);
        TelematicsIngestRequest request = new TelematicsIngestRequest("evt-004", "VEH-LOGIX-101", "NON-EXISTENT-TENANT", now.minusSeconds(2), payload);

        when(tenantRepository.existsById("NON-EXISTENT-TENANT")).thenReturn(false);

        assertThrows(TenantNotFoundException.class, () -> validationService.validate(request));
    }

    @Test
    @DisplayName("Should fail when vehicle ID is not registered in system")
    void validate_VehicleNotFound_ThrowsException() {
        TelematicsPayload payload = new TelematicsPayload(65.0, 80.0, null, null, null, null, null);
        TelematicsIngestRequest request = new TelematicsIngestRequest("evt-005", "NON-EXISTENT-VEHICLE", "TENANT-LOGIX-001", now.minusSeconds(2), payload);

        when(tenantRepository.existsById("TENANT-LOGIX-001")).thenReturn(true);
        when(vehicleRepository.findById("NON-EXISTENT-VEHICLE")).thenReturn(Optional.empty());

        assertThrows(VehicleNotFoundException.class, () -> validationService.validate(request));
    }
}
