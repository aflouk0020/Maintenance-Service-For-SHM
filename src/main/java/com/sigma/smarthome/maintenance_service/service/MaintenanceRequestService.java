package com.sigma.smarthome.maintenance_service.service;

import com.sigma.smarthome.maintenance_service.client.PropertyServiceClient;
import com.sigma.smarthome.maintenance_service.dto.CreateMaintenanceRequestDto;
import com.sigma.smarthome.maintenance_service.dto.MaintenanceRequestResponse;
import com.sigma.smarthome.maintenance_service.entity.MaintenanceHistory;
import com.sigma.smarthome.maintenance_service.entity.MaintenanceRequest;
import com.sigma.smarthome.maintenance_service.exception.ForbiddenOperationException;
import com.sigma.smarthome.maintenance_service.exception.ResourceNotFoundException;
import com.sigma.smarthome.maintenance_service.repository.MaintenanceHistoryRepository;
import com.sigma.smarthome.maintenance_service.repository.MaintenanceRequestRepository;
import org.springframework.stereotype.Service;
import com.sigma.smarthome.maintenance_service.dto.MaintenanceHistoryResponse;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class MaintenanceRequestService {

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("OPEN", "IN_PROGRESS", "COMPLETED");

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final PropertyServiceClient propertyServiceClient;
    private final MaintenanceHistoryRepository maintenanceHistoryRepository;

    public MaintenanceRequestService(MaintenanceRequestRepository maintenanceRequestRepository,
                                     PropertyServiceClient propertyServiceClient,
                                     MaintenanceHistoryRepository maintenanceHistoryRepository) {
        this.maintenanceRequestRepository = maintenanceRequestRepository;
        this.propertyServiceClient = propertyServiceClient;
        this.maintenanceHistoryRepository = maintenanceHistoryRepository;
    }

    public List<MaintenanceRequest> getRequestsForManager(UUID managerId, String bearerToken) {
        List<UUID> propertyIds = propertyServiceClient.getPropertyIdsManagedBy(managerId, bearerToken);

        if (propertyIds.isEmpty()) {
            return List.of();
        }

        return maintenanceRequestRepository.findByPropertyIdIn(propertyIds);
    }

    public List<MaintenanceHistoryResponse> getRequestHistory(UUID requestId) {
        if (!maintenanceRequestRepository.existsById(requestId)) {
            throw new ResourceNotFoundException("Maintenance request not found: " + requestId);
        }

        return maintenanceHistoryRepository.findByRequestIdOrderByChangedAtAsc(requestId)
                .stream()
                .map(history -> new MaintenanceHistoryResponse(
                        history.getId(),
                        history.getRequestId(),
                        history.getOldStatus(),
                        history.getNewStatus(),
                        history.getChangedByUserId(),
                        history.getChangedAt()
                ))
                .collect(Collectors.toList());
    }
    
    public MaintenanceRequestResponse createRequest(CreateMaintenanceRequestDto dto) {
        propertyServiceClient.validatePropertyExists(dto.getPropertyId());

        MaintenanceRequest request = new MaintenanceRequest();
        request.setPropertyId(dto.getPropertyId());
        request.setCreatedByUserId(dto.getCreatedByUserId());
        request.setAssignedStaffId(dto.getCreatedByUserId());
        request.setDescription(dto.getDescription());
        request.setPriority(dto.getPriority());
        request.setStatus("OPEN");

        MaintenanceRequest saved = maintenanceRequestRepository.save(request);

        return new MaintenanceRequestResponse(
                saved.getId(),
                saved.getPropertyId(),
                saved.getCreatedByUserId(),
                saved.getAssignedStaffId(),
                saved.getDescription(),
                saved.getPriority(),
                saved.getStatus(),
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                saved.getCompletedAt()
        );
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

        String oldStatus = request.getStatus();

        request.setStatus(normalizedStatus);

        if ("COMPLETED".equals(normalizedStatus)) {
            request.setCompletedAt(LocalDateTime.now());
        }

        MaintenanceRequest savedRequest = maintenanceRequestRepository.save(request);

        MaintenanceHistory history = new MaintenanceHistory();
        history.setRequestId(savedRequest.getId());
        history.setOldStatus(oldStatus);
        history.setNewStatus(normalizedStatus);
        history.setChangedByUserId(loggedInUserId);

        maintenanceHistoryRepository.save(history);

        return savedRequest;
    }

    public MaintenanceRequestResponse getRequestById(UUID id) {
        MaintenanceRequest request = maintenanceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance request not found: " + id));

        return new MaintenanceRequestResponse(
                request.getId(),
                request.getPropertyId(),
                request.getCreatedByUserId(),
                request.getAssignedStaffId(),
                request.getDescription(),
                request.getPriority(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getCompletedAt()
        );
    }
}