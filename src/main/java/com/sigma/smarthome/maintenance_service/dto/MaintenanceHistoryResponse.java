package com.sigma.smarthome.maintenance_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class MaintenanceHistoryResponse {

    private UUID id;
    private UUID requestId;
    private String oldStatus;
    private String newStatus;
    private UUID changedByUserId;
    private LocalDateTime changedAt;

    public MaintenanceHistoryResponse() {
    }

    public MaintenanceHistoryResponse(UUID id, UUID requestId, String oldStatus, String newStatus,
                                      UUID changedByUserId, LocalDateTime changedAt) {
        this.id = id;
        this.requestId = requestId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedByUserId = changedByUserId;
        this.changedAt = changedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public UUID getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(UUID changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}