package com.sigma.smarthome.maintenance_service.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AssignStaffDto {

    @NotNull(message = "staffId is required")
    private UUID staffId;

    public UUID getStaffId() {
        return staffId;
    }

    public void setStaffId(UUID staffId) {
        this.staffId = staffId;
    }
}