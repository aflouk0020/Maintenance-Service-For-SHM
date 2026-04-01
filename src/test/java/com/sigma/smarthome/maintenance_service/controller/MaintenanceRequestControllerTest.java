package com.sigma.smarthome.maintenance_service.controller;

import com.sigma.smarthome.maintenance_service.dto.CreateMaintenanceRequestDto;
import com.sigma.smarthome.maintenance_service.dto.MaintenanceRequestResponse;
import com.sigma.smarthome.maintenance_service.dto.UpdateMaintenanceStatusDto;
import com.sigma.smarthome.maintenance_service.entity.MaintenanceRequest;
import com.sigma.smarthome.maintenance_service.exception.ForbiddenOperationException;
import com.sigma.smarthome.maintenance_service.exception.ResourceNotFoundException;
import com.sigma.smarthome.maintenance_service.service.MaintenanceRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class MaintenanceRequestControllerTest {

    private MaintenanceRequestService maintenanceRequestService;
    private MaintenanceRequestController maintenanceRequestController;

    @BeforeEach
    void setUp() {
        maintenanceRequestService = Mockito.mock(MaintenanceRequestService.class);
        maintenanceRequestController = new MaintenanceRequestController(maintenanceRequestService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateStatus_ShouldReturn200_WhenAssignedUserUpdatesOwnRequest() {
        UUID requestId = UUID.randomUUID();
        UUID loggedInUserId = UUID.randomUUID();

        UpdateMaintenanceStatusDto dto = new UpdateMaintenanceStatusDto();
        dto.setStatus("IN_PROGRESS");

        MaintenanceRequest updated = new MaintenanceRequest();
        updated.setId(requestId);
        updated.setCreatedByUserId(loggedInUserId);
        updated.setAssignedStaffId(loggedInUserId);
        updated.setStatus("IN_PROGRESS");

        when(maintenanceRequestService.updateStatus(requestId, loggedInUserId, "IN_PROGRESS"))
                .thenReturn(updated);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        loggedInUserId.toString(),
                        null,
                        List.of()
                )
        );

        ResponseEntity<MaintenanceRequest> response =
                maintenanceRequestController.updateStatus(requestId, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("IN_PROGRESS", response.getBody().getStatus());
        assertEquals(requestId, response.getBody().getId());
    }

    @Test
    void updateStatus_ShouldThrowForbidden_WhenUserTriesToUpdateUnassignedRequest() {
        UUID requestId = UUID.randomUUID();
        UUID loggedInUserId = UUID.randomUUID();

        UpdateMaintenanceStatusDto dto = new UpdateMaintenanceStatusDto();
        dto.setStatus("IN_PROGRESS");

        when(maintenanceRequestService.updateStatus(requestId, loggedInUserId, "IN_PROGRESS"))
                .thenThrow(new ForbiddenOperationException("You are not allowed to update this maintenance request"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        loggedInUserId.toString(),
                        null,
                        List.of()
                )
        );

        ForbiddenOperationException ex = assertThrows(
                ForbiddenOperationException.class,
                () -> maintenanceRequestController.updateStatus(requestId, dto)
        );

        assertEquals("You are not allowed to update this maintenance request", ex.getMessage());
    }

    @Test
    void updateStatus_ShouldThrowNotFound_WhenRequestDoesNotExist() {
        UUID requestId = UUID.randomUUID();
        UUID loggedInUserId = UUID.randomUUID();

        UpdateMaintenanceStatusDto dto = new UpdateMaintenanceStatusDto();
        dto.setStatus("IN_PROGRESS");

        when(maintenanceRequestService.updateStatus(requestId, loggedInUserId, "IN_PROGRESS"))
                .thenThrow(new ResourceNotFoundException("Maintenance request not found: " + requestId));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        loggedInUserId.toString(),
                        null,
                        List.of()
                )
        );

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> maintenanceRequestController.updateStatus(requestId, dto)
        );

        assertEquals("Maintenance request not found: " + requestId, ex.getMessage());
    }

    @Test
    void createRequest_ShouldReturn201_WhenValidRequest() {
        UUID propertyId = UUID.randomUUID();
        UUID createdByUserId = UUID.randomUUID();

        CreateMaintenanceRequestDto dto = new CreateMaintenanceRequestDto();
        dto.setPropertyId(propertyId);
        dto.setCreatedByUserId(createdByUserId);
        dto.setDescription("Leaking pipe");
        dto.setPriority("HIGH");

        MaintenanceRequestResponse response = new MaintenanceRequestResponse(
                UUID.randomUUID(), propertyId, createdByUserId,
                null, "Leaking pipe", "HIGH", "OPEN",
                null, null, null
        );

        when(maintenanceRequestService.createRequest(dto)).thenReturn(response);

        ResponseEntity<MaintenanceRequestResponse> result =
                maintenanceRequestController.createRequest(dto);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals("OPEN", result.getBody().getStatus());
        assertEquals(propertyId, result.getBody().getPropertyId());
    }

    @Test
    void createRequest_ShouldThrowNotFound_WhenPropertyDoesNotExist() {
        UUID propertyId = UUID.randomUUID();
        UUID createdByUserId = UUID.randomUUID();

        CreateMaintenanceRequestDto dto = new CreateMaintenanceRequestDto();
        dto.setPropertyId(propertyId);
        dto.setCreatedByUserId(createdByUserId);
        dto.setDescription("Leaking pipe");
        dto.setPriority("HIGH");

        when(maintenanceRequestService.createRequest(dto))
                .thenThrow(new ResourceNotFoundException("Property not found: " + propertyId));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> maintenanceRequestController.createRequest(dto)
        );

        assertEquals("Property not found: " + propertyId, ex.getMessage());
    }
}