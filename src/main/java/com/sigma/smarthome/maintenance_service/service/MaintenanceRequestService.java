package com.sigma.smarthome.maintenance_service.service;

import com.sigma.smarthome.maintenance_service.client.PropertyServiceClient;
import com.sigma.smarthome.maintenance_service.dto.CreateMaintenanceRequestDto;
import com.sigma.smarthome.maintenance_service.entity.MaintenanceRequest;
import com.sigma.smarthome.maintenance_service.exception.ForbiddenOperationException;
import com.sigma.smarthome.maintenance_service.exception.ResourceNotFoundException;
import com.sigma.smarthome.maintenance_service.repository.MaintenanceRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
public class MaintenanceRequestService {

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("OPEN", "IN_PROGRESS", "COMPLETED");

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final PropertyServiceClient propertyServiceClient;

    public MaintenanceRequestService(MaintenanceRequestRepository maintenanceRequestRepository,
                                     PropertyServiceClient propertyServiceClient) {
        this.maintenanceRequestRepository = maintenanceRequestRepository;
        this.propertyServiceClient = propertyServiceClient;
    }

    public MaintenanceRequest createRequest(CreateMaintenanceRequestDto dto) {
        propertyServiceClient.validatePropertyExists(dto.getPropertyId());

        MaintenanceRequest request = new MaintenanceRequest();
        request.setPropertyId(dto.getPropertyId());
        request.setCreatedByUserId(dto.getCreatedByUserId());
        request.setAssignedStaffId(dto.getCreatedByUserId()); // for Story 3.3 local validation
        request.setDescription(dto.getDescription());
        request.setPriority(dto.getPriority());
        request.setStatus("OPEN");

        return maintenanceRequestRepository.save(request);
    }

    public MaintenanceRequest updateStatus(UUID requestId, UUID loggedInUserId, String newStatus) {
        MaintenanceRequest request = maintenanceRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Maintenance request not found: " + requestId));

        if (request.getAssignedStaffId() == null || !request.getAssignedStaffId().equals(loggedInUserId)) {
            throw new ForbiddenOperationException(
                    "You are not allowed to update this maintenance request");
        }

        String normalizedStatus = newStatus.trim().toUpperCase();

        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        request.setStatus(normalizedStatus);

        if ("COMPLETED".equals(normalizedStatus)) {
            request.setCompletedAt(LocalDateTime.now());
        }

        return maintenanceRequestRepository.save(request);
    }
}