package com.fleet.telematics.service;

import com.fleet.telematics.domain.entity.Tenant;
import com.fleet.telematics.domain.entity.TelematicsEvent;
import com.fleet.telematics.domain.entity.Vehicle;
import com.fleet.telematics.dto.response.TenantResponse;
import com.fleet.telematics.dto.response.VehicleStatusResponse;
import com.fleet.telematics.exception.TenantNotFoundException;
import com.fleet.telematics.exception.VehicleNotFoundException;
import com.fleet.telematics.repository.TenantRepository;
import com.fleet.telematics.repository.TelematicsEventRepository;
import com.fleet.telematics.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FleetQueryService {

    private final TenantRepository tenantRepository;
    private final VehicleRepository vehicleRepository;
    private final TelematicsEventRepository eventRepository;

    public FleetQueryService(TenantRepository tenantRepository,
                             VehicleRepository vehicleRepository,
                             TelematicsEventRepository eventRepository) {
        this.tenantRepository = tenantRepository;
        this.vehicleRepository = vehicleRepository;
        this.eventRepository = eventRepository;
    }

    public List<TenantResponse> getAllTenantsWithVehicles() {
        return tenantRepository.findAll().stream()
                .map(this::mapToTenantResponse)
                .collect(Collectors.toList());
    }

    public TenantResponse getTenantById(String tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));
        return mapToTenantResponse(tenant);
    }

    public List<VehicleStatusResponse> getVehiclesByTenant(String tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new TenantNotFoundException(tenantId);
        }
        return vehicleRepository.findByTenantId(tenantId).stream()
                .map(this::mapToVehicleStatusResponse)
                .collect(Collectors.toList());
    }

    public VehicleStatusResponse getVehicleStatus(String vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));
        return mapToVehicleStatusResponse(vehicle);
    }

    public Page<TelematicsEvent> getEvents(String tenantId, String vehicleId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventTimestamp"));

        if (vehicleId != null && !vehicleId.isBlank()) {
            return eventRepository.findByTenantIdAndVehicleId(tenantId, vehicleId, pageable);
        }
        return eventRepository.findByTenantId(tenantId, pageable);
    }

    private TenantResponse mapToTenantResponse(Tenant tenant) {
        List<VehicleStatusResponse> vehicleResponses = vehicleRepository.findByTenantId(tenant.getId())
                .stream()
                .map(this::mapToVehicleStatusResponse)
                .collect(Collectors.toList());

        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getContactEmail(),
                tenant.getStatus(),
                vehicleResponses
        );
    }

    private VehicleStatusResponse mapToVehicleStatusResponse(Vehicle vehicle) {
        VehicleStatusResponse response = new VehicleStatusResponse(
                vehicle.getId(),
                vehicle.getTenant().getId(),
                vehicle.getTenant().getName(),
                vehicle.getVin(),
                vehicle.getModel(),
                vehicle.getStatus()
        );

        Optional<TelematicsEvent> latestEvent = eventRepository.findFirstByVehicleIdOrderByEventTimestampDesc(vehicle.getId());
        latestEvent.ifPresent(evt -> {
            response.setLastKnownSpeed(evt.getSpeed());
            response.setLastKnownFuelLevel(evt.getFuelLevel());
            response.setLastSeenTimestamp(evt.getEventTimestamp());
        });

        return response;
    }
}
