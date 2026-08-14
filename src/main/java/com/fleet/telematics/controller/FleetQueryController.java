package com.fleet.telematics.controller;

import com.fleet.telematics.domain.entity.TelematicsEvent;
import com.fleet.telematics.dto.response.TenantResponse;
import com.fleet.telematics.dto.response.VehicleStatusResponse;
import com.fleet.telematics.service.FleetQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Fleet Query & Administration", description = "Query tenants, authorized vehicles, and historical telemetry data")
public class FleetQueryController {

    private final FleetQueryService fleetQueryService;

    public FleetQueryController(FleetQueryService fleetQueryService) {
        this.fleetQueryService = fleetQueryService;
    }

    @GetMapping("/tenants")
    @Operation(summary = "Get All Tenants", description = "List all registered tenant platform accounts and their assigned vehicles.")
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(fleetQueryService.getAllTenantsWithVehicles());
    }

    @GetMapping("/tenants/{tenantId}")
    @Operation(summary = "Get Tenant Details", description = "Get specific tenant platform account and assigned vehicles.")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable String tenantId) {
        return ResponseEntity.ok(fleetQueryService.getTenantById(tenantId));
    }

    @GetMapping("/tenants/{tenantId}/vehicles")
    @Operation(summary = "Get Vehicles by Tenant", description = "List authorized vehicles owned by a specific tenant.")
    public ResponseEntity<List<VehicleStatusResponse>> getVehiclesByTenant(@PathVariable String tenantId) {
        return ResponseEntity.ok(fleetQueryService.getVehiclesByTenant(tenantId));
    }

    @GetMapping("/vehicles/{vehicleId}")
    @Operation(summary = "Get Vehicle Status", description = "Retrieve vehicle details and latest known telemetry snapshot.")
    public ResponseEntity<VehicleStatusResponse> getVehicleStatus(@PathVariable String vehicleId) {
        return ResponseEntity.ok(fleetQueryService.getVehicleStatus(vehicleId));
    }

    @GetMapping("/telemetry/events")
    @Operation(summary = "Query Telemetry Events", description = "Fetch paginated telemetry events filtered by tenantId and optional vehicleId.")
    public ResponseEntity<Page<TelematicsEvent>> getEvents(
            @RequestParam String tenantId,
            @RequestParam(required = false) String vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(fleetQueryService.getEvents(tenantId, vehicleId, page, size));
    }
}
