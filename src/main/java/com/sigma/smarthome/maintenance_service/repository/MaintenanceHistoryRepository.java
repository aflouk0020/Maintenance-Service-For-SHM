package com.sigma.smarthome.maintenance_service.repository;

import com.sigma.smarthome.maintenance_service.entity.MaintenanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MaintenanceHistoryRepository extends JpaRepository<MaintenanceHistory, UUID> {
    List<MaintenanceHistory> findByRequestIdOrderByChangedAtAsc(UUID requestId);
}