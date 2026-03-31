package com.sigma.smarthome.maintenance_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class MaintenanceRequestResponse {

    private UUID id;
    private UUID propertyId;
    private UUID createdByUserId;
    private UUID assignedStaffId;
    private String description;
    private String priority;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    public MaintenanceRequestResponse(UUID id, UUID propertyId, UUID createdByUserId,
                                      UUID assignedStaffId, String description, String priority,
                                      String status, LocalDateTime createdAt, LocalDateTime updatedAt,
                                      LocalDateTime completedAt) {
        this.id = id;
        this.propertyId = propertyId;
        this.createdByUserId = createdByUserId;
        this.assignedStaffId = assignedStaffId;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
    }

    public UUID getId() { return id; }
    public UUID getPropertyId() { return propertyId; }
    public UUID getCreatedByUserId() { return createdByUserId; }
    public UUID getAssignedStaffId() { return assignedStaffId; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}