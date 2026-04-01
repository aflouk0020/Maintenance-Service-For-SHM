package com.sigma.smarthome.maintenance_service.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateMaintenanceStatusDto {

    @NotBlank(message = "status is required")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}