package com.sigma.smarthome.maintenance_service.client;

import com.sigma.smarthome.maintenance_service.exception.ForbiddenOperationException;
import com.sigma.smarthome.maintenance_service.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Component
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    public UserServiceClient(RestTemplate restTemplate,
                             @Value("${user.service.base-url}") String userServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    public void validateMaintenanceStaff(UUID userId, String bearerToken) {
        String url = userServiceBaseUrl + "/api/v1/users/" + userId + "/role";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            Object role = response.getBody() != null ? response.getBody().get("role") : null;

            if (role == null) {
                throw new ResourceNotFoundException("Role information not found for user: " + userId);
            }

            if (!"MAINTENANCE_STAFF".equals(role.toString())) {
                throw new ForbiddenOperationException("Only MAINTENANCE_STAFF users can be assigned");
            }

        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("User not found: " + userId);
        } catch (HttpClientErrorException.Unauthorized ex) {
            throw new ForbiddenOperationException("Unauthorized to validate selected user");
        } catch (HttpClientErrorException.Forbidden ex) {
            throw new ForbiddenOperationException("Forbidden from validating selected user");
        }
    }
}