package com.sigma.smarthome.maintenance_service.repository;

import com.sigma.smarthome.maintenance_service.entity.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, UUID> {

    List<MaintenanceRequest> findByPropertyIdIn(List<UUID> propertyIds);
    List<MaintenanceRequest> findByPropertyIdInAndStatus(List<UUID> propertyIds, String status);

    List<MaintenanceRequest> findByPropertyIdInAndPriority(List<UUID> propertyIds, String priority);

    List<MaintenanceRequest> findByPropertyIdInAndStatusAndPriority(List<UUID> propertyIds, String status, String priority);
}