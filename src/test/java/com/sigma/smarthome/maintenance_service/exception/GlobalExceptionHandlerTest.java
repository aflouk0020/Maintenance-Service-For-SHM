package com.sigma.smarthome.maintenance_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleServiceUnavailable_ShouldReturnFriendlyMessage() {
        ServiceUnavailableException ex =
                new ServiceUnavailableException("Property Service is unavailable");

        ResponseEntity<Map<String, Object>> response = handler.handleServiceUnavailable(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(503, response.getBody().get("status"));
        assertEquals("Service Unavailable", response.getBody().get("error"));
        assertEquals(
                "A required service is temporarily unavailable. Please try again shortly.",
                response.getBody().get("message")
        );
    }

    @Test
    void handleGeneral_ShouldReturnFriendlyGenericMessage() {
        Exception ex = new Exception("Low-level internal failure");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().get("status"));
        assertEquals("Internal Server Error", response.getBody().get("error"));
        assertEquals(
                "An unexpected error occurred. Please try again later.",
                response.getBody().get("message")
        );
    }

    @Test
    void handleValidation_ShouldReturn400_WhenRequiredFieldMissing() {
        org.springframework.validation.BeanPropertyBindingResult bindingResult =
                new org.springframework.validation.BeanPropertyBindingResult(
                        new com.sigma.smarthome.maintenance_service.dto.CreateMaintenanceRequestDto(),
                        "dto"
                );
        bindingResult.rejectValue("description", "NotBlank", "description is required");

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("description is required", response.getBody().get("message"));
    }

    @Test
    void handleNotFound_ShouldReturn404_WhenResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Maintenance request not found: 123");

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Not Found", response.getBody().get("error"));
        assertEquals("Maintenance request not found: 123", response.getBody().get("message"));
    }

    @Test
    void handleForbidden_ShouldReturn403_WhenForbiddenOperation() {
        ForbiddenOperationException ex = new ForbiddenOperationException("You are not allowed to update this maintenance request");

        ResponseEntity<Map<String, Object>> response = handler.handleForbidden(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().get("status"));
        assertEquals("Forbidden", response.getBody().get("error"));
        assertEquals("You are not allowed to update this maintenance request", response.getBody().get("message"));
    }
}