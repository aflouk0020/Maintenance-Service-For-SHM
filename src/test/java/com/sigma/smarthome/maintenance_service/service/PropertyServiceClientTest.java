package com.sigma.smarthome.maintenance_service.service;

import com.sigma.smarthome.maintenance_service.client.PropertyServiceClient;
import com.sigma.smarthome.maintenance_service.exception.ResourceNotFoundException;
import com.sigma.smarthome.maintenance_service.exception.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    private PropertyServiceClient propertyServiceClient;

    private UUID propertyId;

    @BeforeEach
    void setUp() {
        propertyServiceClient = new PropertyServiceClient(restTemplate, "http://localhost:8082");
        propertyId = UUID.randomUUID();
    }

    @Test
    void validatePropertyExists_ShouldSucceed_WhenPropertyExists() {
        String url = "http://localhost:8082/api/v1/properties/" + propertyId + "/exists";

        when(restTemplate.getForEntity(url, Void.class))
                .thenReturn(ResponseEntity.ok().build());

        // Should not throw
        propertyServiceClient.validatePropertyExists(propertyId);
    }

    @Test
    void validatePropertyExists_ShouldThrowResourceNotFound_WhenPropertyDoesNotExist() {
        String url = "http://localhost:8082/api/v1/properties/" + propertyId + "/exists";

        when(restTemplate.getForEntity(url, Void.class))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThrows(ResourceNotFoundException.class,
                () -> propertyServiceClient.validatePropertyExists(propertyId));
    }

    @Test
    void validatePropertyExists_ShouldThrowServiceUnavailable_WhenPropertyServiceIsDown() {
        String url = "http://localhost:8082/api/v1/properties/" + propertyId + "/exists";

        when(restTemplate.getForEntity(url, Void.class))
                .thenThrow(new RestClientException("Connection refused"));

        assertThrows(ServiceUnavailableException.class,
                () -> propertyServiceClient.validatePropertyExists(propertyId));
    }
}