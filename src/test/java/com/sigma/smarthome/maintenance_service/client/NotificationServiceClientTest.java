package com.sigma.smarthome.maintenance_service.client;

import com.sigma.smarthome.maintenance_service.dto.CreateNotificationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class NotificationServiceClientTest {

    private RestTemplate restTemplate;
    private NotificationServiceClient notificationServiceClient;

    @BeforeEach
    void setUp() {
        restTemplate = Mockito.mock(RestTemplate.class);
        notificationServiceClient =
                new NotificationServiceClient(restTemplate, "http://localhost:8084");
    }

    @Test
    void sendNotification_ShouldNotThrow_WhenNotificationServiceFails() {
        CreateNotificationDto dto = new CreateNotificationDto(
                UUID.randomUUID(),
                "Maintenance Request Updated",
                "Your request has been updated",
                "STATUS_UPDATE",
                false
        );

        Mockito.when(restTemplate.postForObject(
                eq("http://localhost:8084/api/v1/notifications"),
                eq(dto),
                eq(Object.class)
        )).thenThrow(new RestClientException("Notification service unavailable"));

        assertDoesNotThrow(() -> notificationServiceClient.sendNotification(dto));
    }
}