package com.fleet.telematics.repository;

import com.fleet.telematics.domain.entity.TelematicsEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelematicsEventRepository extends JpaRepository<TelematicsEvent, Long> {

    boolean existsByEventId(String eventId);

    Optional<TelematicsEvent> findByEventId(String eventId);

    Page<TelematicsEvent> findByTenantId(String tenantId, Pageable pageable);

    Page<TelematicsEvent> findByTenantIdAndVehicleId(String tenantId, String vehicleId, Pageable pageable);

    Optional<TelematicsEvent> findFirstByVehicleIdOrderByEventTimestampDesc(String vehicleId);
}
