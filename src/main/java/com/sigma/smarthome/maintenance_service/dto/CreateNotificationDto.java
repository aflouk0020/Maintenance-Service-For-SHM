package com.sigma.smarthome.maintenance_service.dto;

import java.util.UUID;

public class CreateNotificationDto {

    private UUID userId;
    private String title;
    private String message;
    private String type;
    private Boolean isRead;

    public CreateNotificationDto() {
    }

    public CreateNotificationDto(UUID userId, String title, String message, String type, Boolean isRead) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean read) {
        isRead = read;
    }
}