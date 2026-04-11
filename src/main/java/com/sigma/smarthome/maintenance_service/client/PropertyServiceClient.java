package com.sigma.smarthome.maintenance_service.client;

import com.sigma.smarthome.maintenance_service.exception.ResourceNotFoundException;
import com.sigma.smarthome.maintenance_service.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
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
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Property Service is unavailable", ex);
        }
    }

    public List<UUID> getPropertyIdsManagedBy(UUID managerId, String bearerToken) {
        String url = propertyServiceBaseUrl + "/api/v1/properties/manager/" + managerId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken.replace("Bearer ", ""));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<UUID[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UUID[].class
            );

            UUID[] body = response.getBody();
            return body == null ? List.of() : Arrays.asList(body);

        } catch (HttpClientErrorException.NotFound ex) {
            return List.of();
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Property Service is unavailable", ex);
        }
    }
}