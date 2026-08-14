package com.fleet.telematics.service;

import com.fleet.telematics.domain.entity.Tenant;
import com.fleet.telematics.domain.entity.Vehicle;
import com.fleet.telematics.repository.TenantRepository;
import com.fleet.telematics.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeederService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeederService.class);

    private final TenantRepository tenantRepository;
    private final VehicleRepository vehicleRepository;

    public DataSeederService(TenantRepository tenantRepository, VehicleRepository vehicleRepository) {
        this.tenantRepository = tenantRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking baseline system data initialization...");

        if (tenantRepository.count() == 0) {
            log.info("Seeding initial baseline Tenants and Vehicles...");

            Tenant tenant1 = new Tenant("TENANT-LOGIX-001", "LogiX Logistics Corp", "ops@logix-logistics.com", "ACTIVE");
            Tenant tenant2 = new Tenant("TENANT-SWIFT-002", "Swift Express Fleet", "fleet@swiftexpress.com", "ACTIVE");

            tenantRepository.save(tenant1);
            tenantRepository.save(tenant2);

            Vehicle v101 = new Vehicle("VEH-LOGIX-101", tenant1, "1HGCR2F83HA000101", "Volvo FH16 Semi-Truck", "ACTIVE");
            Vehicle v102 = new Vehicle("VEH-LOGIX-102", tenant1, "1HGCR2F83HA000102", "Scania R500 Heavy Hauler", "ACTIVE");
            Vehicle v103 = new Vehicle("VEH-LOGIX-103", tenant1, "1HGCR2F83HA000103", "Freightliner Cascadia EV", "ACTIVE");

            Vehicle v201 = new Vehicle("VEH-SWIFT-201", tenant2, "2C3CDZFJ9KH000201", "Mercedes-Benz Actros 1845", "ACTIVE");
            Vehicle v202 = new Vehicle("VEH-SWIFT-202", tenant2, "2C3CDZFJ9KH000202", "DAF XF 530 Super Space", "ACTIVE");

            vehicleRepository.save(v101);
            vehicleRepository.save(v102);
            vehicleRepository.save(v103);
            vehicleRepository.save(v201);
            vehicleRepository.save(v202);

            log.info("System successfully seeded with 2 Tenants and 5 Vehicles.");
        } else {
            log.info("Baseline system data already exists: {} tenants, {} vehicles.",
                    tenantRepository.count(), vehicleRepository.count());
        }
    }
}
