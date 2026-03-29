package com.sigma.smarthome.maintenance_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateMaintenanceRequestDto {

    @NotNull(message = "propertyId is required")
    private UUID propertyId;

    @NotNull(message = "createdByUserId is required")
    private UUID createdByUserId;

    @NotBlank(message = "description is required")
    private String description;

    @NotBlank(message = "priority is required")
    private String priority;

    public UUID getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(UUID propertyId) {
        this.propertyId = propertyId;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(UUID createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}