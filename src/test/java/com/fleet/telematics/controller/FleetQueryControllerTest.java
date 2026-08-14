package com.fleet.telematics.controller;

import com.fleet.telematics.dto.response.TenantResponse;
import com.fleet.telematics.dto.response.VehicleStatusResponse;
import com.fleet.telematics.exception.GlobalExceptionHandler;
import com.fleet.telematics.service.FleetQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {FleetQueryController.class, GlobalExceptionHandler.class})
class FleetQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FleetQueryService fleetQueryService;

    @Test
    @DisplayName("GET /api/v1/tenants - Should return list of pre-populated tenants")
    void getAllTenants_Success() throws Exception {
        TenantResponse tenant1 = new TenantResponse("TENANT-LOGIX-001", "LogiX Logistics Corp", "ops@logix.com", "ACTIVE", List.of());
        TenantResponse tenant2 = new TenantResponse("TENANT-SWIFT-002", "Swift Express Fleet", "fleet@swift.com", "ACTIVE", List.of());

        when(fleetQueryService.getAllTenantsWithVehicles()).thenReturn(List.of(tenant1, tenant2));

        mockMvc.perform(get("/api/v1/tenants")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tenantId", is("TENANT-LOGIX-001")))
                .andExpect(jsonPath("$[1].tenantId", is("TENANT-SWIFT-002")));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles/{vehicleId} - Should return vehicle status details")
    void getVehicleStatus_Success() throws Exception {
        VehicleStatusResponse vehicleStatus = new VehicleStatusResponse("VEH-LOGIX-101", "TENANT-LOGIX-001", "LogiX Logistics Corp", "VIN101", "Volvo FH16", "ACTIVE");
        vehicleStatus.setLastKnownSpeed(72.5);
        vehicleStatus.setLastKnownFuelLevel(88.0);

        when(fleetQueryService.getVehicleStatus("VEH-LOGIX-101")).thenReturn(vehicleStatus);

        mockMvc.perform(get("/api/v1/vehicles/VEH-LOGIX-101")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId", is("VEH-LOGIX-101")))
                .andExpect(jsonPath("$.lastKnownSpeed", is(72.5)))
                .andExpect(jsonPath("$.lastKnownFuelLevel", is(88.0)));
    }
}
