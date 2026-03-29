package com.sigma.smarthome.maintenance_service.client;

import com.sigma.smarthome.maintenance_service.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class PropertyServiceClient {

    private final RestTemplate restTemplate;
    private final String propertyServiceBaseUrl;

    public PropertyServiceClient(RestTemplate restTemplate,
                                 @Value("${property.service.base-url}") String propertyServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.propertyServiceBaseUrl = propertyServiceBaseUrl;
    }

    public void validatePropertyExists(UUID propertyId) {
        String url = propertyServiceBaseUrl + "/api/v1/properties/" + propertyId + "/exists";

        try {
            ResponseEntity<Void> response = restTemplate.getForEntity(url, Void.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ResourceNotFoundException("Property not found: " + propertyId);
            }

        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Property not found: " + propertyId);
        } catch (Exception ex) {
            throw new RuntimeException("Property Service unavailable: " + ex.getMessage());
        }
    }
}