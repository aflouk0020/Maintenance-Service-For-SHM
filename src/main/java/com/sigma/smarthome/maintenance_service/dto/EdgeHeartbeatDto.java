package com.sigma.smarthome.maintenance_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EdgeHeartbeatDto {

    @NotBlank(message = "deviceId is required")
    private String deviceId;

    @NotBlank(message = "propertyId is required")
    private String propertyId;

    @NotBlank(message = "status is required")
    private String status;

    @NotNull(message = "temperature is required")
    private Double temperature;

    @NotNull(message = "humidity is required")
    private Double humidity;

    @NotBlank(message = "timestamp is required")
    private String timestamp;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}