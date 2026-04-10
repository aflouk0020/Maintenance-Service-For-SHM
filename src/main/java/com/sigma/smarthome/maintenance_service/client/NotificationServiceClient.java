package com.sigma.smarthome.maintenance_service.client;

import com.sigma.smarthome.maintenance_service.dto.CreateNotificationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class NotificationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceClient.class);

    private final RestTemplate restTemplate;
    private final String notificationServiceBaseUrl;

    public NotificationServiceClient(RestTemplate restTemplate,
                                     @Value("${notification.service.base-url}") String notificationServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.notificationServiceBaseUrl = notificationServiceBaseUrl;
    }

    public void sendNotification(CreateNotificationDto dto) {
        try {
            restTemplate.postForObject(
                    notificationServiceBaseUrl + "/api/v1/notifications",
                    dto,
                    Object.class
            );
        } catch (RestClientException ex) {
            log.warn("Notification Service unavailable. Main workflow continues. Reason: {}", ex.getMessage());
        }
    }
}