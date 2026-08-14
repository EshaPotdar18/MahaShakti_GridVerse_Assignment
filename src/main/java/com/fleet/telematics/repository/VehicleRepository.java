package com.fleet.telematics.repository;

import com.fleet.telematics.domain.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    List<Vehicle> findByTenantId(String tenantId);

    Optional<Vehicle> findByIdAndTenantId(String id, String tenantId);
}
