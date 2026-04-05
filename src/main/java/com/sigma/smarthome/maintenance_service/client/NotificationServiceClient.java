package com.sigma.smarthome.maintenance_service.client;

import com.sigma.smarthome.maintenance_service.dto.CreateNotificationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NotificationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceClient.class);

    private final RestTemplate restTemplate;

    public NotificationServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendNotification(CreateNotificationDto dto) {
        try {
            restTemplate.postForObject(
                    "http://localhost:8084/api/v1/notifications",
                    dto,
                    Object.class
            );
        } catch (Exception ex) {
            log.error("Failed to send notification: {}", ex.getMessage());
        }
    }
}