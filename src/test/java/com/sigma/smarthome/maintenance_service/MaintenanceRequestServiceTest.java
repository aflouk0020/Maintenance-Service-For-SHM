package com.sigma.smarthome.maintenance_service;

import com.sigma.smarthome.maintenance_service.client.PropertyServiceClient;
import com.sigma.smarthome.maintenance_service.dto.CreateMaintenanceRequestDto;
import com.sigma.smarthome.maintenance_service.dto.MaintenanceRequestResponse;
import com.sigma.smarthome.maintenance_service.entity.MaintenanceRequest;
import com.sigma.smarthome.maintenance_service.exception.ForbiddenOperationException;
import com.sigma.smarthome.maintenance_service.exception.ResourceNotFoundException;
import com.sigma.smarthome.maintenance_service.repository.MaintenanceRequestRepository;
import com.sigma.smarthome.maintenance_service.service.MaintenanceRequestService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaintenanceRequestServiceTest {

    @Mock
    private MaintenanceRequestRepository maintenanceRequestRepository;

    @Mock
    private PropertyServiceClient propertyServiceClient;

    @InjectMocks
    private MaintenanceRequestService maintenanceRequestService;

    private UUID propertyId;
    private UUID createdByUserId;
    private UUID requestId;

    @BeforeEach
    void setUp() {
        propertyId = UUID.randomUUID();
        createdByUserId = UUID.randomUUID();
        requestId = UUID.randomUUID();
    }

    @Test
    void createRequest_ShouldValidatePropertyAndSaveRequest() {
        CreateMaintenanceRequestDto dto = new CreateMaintenanceRequestDto();
        dto.setPropertyId(propertyId);
        dto.setCreatedByUserId(createdByUserId);
        dto.setDescription("Leaking pipe");
        dto.setPriority("HIGH");

        MaintenanceRequest saved = new MaintenanceRequest();
        saved.setId(requestId);
        saved.setPropertyId(propertyId);
        saved.setCreatedByUserId(createdByUserId);
        saved.setAssignedStaffId(createdByUserId);
        saved.setDescription("Leaking pipe");
        saved.setPriority("HIGH");
        saved.setStatus("OPEN");

        when(maintenanceRequestRepository.save(org.mockito.ArgumentMatchers.any(MaintenanceRequest.class)))
                .thenReturn(saved);

        MaintenanceRequestResponse result = maintenanceRequestService.createRequest(dto);

        verify(propertyServiceClient).validatePropertyExists(propertyId);
        verify(maintenanceRequestRepository).save(org.mockito.ArgumentMatchers.any(MaintenanceRequest.class));

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals("OPEN", result.getStatus());
        assertEquals(createdByUserId, result.getAssignedStaffId());
    }

    @Test
    void updateStatus_ShouldUpdateStatus_WhenAssignedToLoggedInUser() {
        UUID loggedInUserId = UUID.randomUUID();

        MaintenanceRequest existing = new MaintenanceRequest();
        existing.setId(requestId);
        existing.setAssignedStaffId(loggedInUserId);
        existing.setStatus("OPEN");

        MaintenanceRequest updated = new MaintenanceRequest();
        updated.setId(requestId);
        updated.setAssignedStaffId(loggedInUserId);
        updated.setStatus("IN_PROGRESS");

        when(maintenanceRequestRepository.findById(requestId)).thenReturn(Optional.of(existing));
        when(maintenanceRequestRepository.save(existing)).thenReturn(updated);

        MaintenanceRequest result =
                maintenanceRequestService.updateStatus(requestId, loggedInUserId, "IN_PROGRESS");

        assertEquals("IN_PROGRESS", result.getStatus());
        verify(maintenanceRequestRepository).findById(requestId);
        verify(maintenanceRequestRepository).save(existing);
    }

    @Test
    void updateStatus_ShouldThrowForbidden_WhenRequestAssignedToDifferentUser() {
        UUID loggedInUserId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();

        MaintenanceRequest existing = new MaintenanceRequest();
        existing.setId(requestId);
        existing.setAssignedStaffId(anotherUserId);
        existing.setStatus("OPEN");

        when(maintenanceRequestRepository.findById(requestId)).thenReturn(Optional.of(existing));

        ForbiddenOperationException ex = assertThrows(
                ForbiddenOperationException.class,
                () -> maintenanceRequestService.updateStatus(requestId, loggedInUserId, "IN_PROGRESS")
        );

        assertEquals("You are not allowed to update this maintenance request", ex.getMessage());
        verify(maintenanceRequestRepository).findById(requestId);
    }

    @Test
    void updateStatus_ShouldThrowNotFound_WhenRequestDoesNotExist() {
        UUID loggedInUserId = UUID.randomUUID();

        when(maintenanceRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> maintenanceRequestService.updateStatus(requestId, loggedInUserId, "IN_PROGRESS")
        );

        assertEquals("Maintenance request not found: " + requestId, ex.getMessage());
        verify(maintenanceRequestRepository).findById(requestId);
    }

    @Test
    void createRequest_ShouldThrowException_WhenPropertyDoesNotExist() {
        CreateMaintenanceRequestDto dto = new CreateMaintenanceRequestDto();
        dto.setPropertyId(propertyId);
        dto.setCreatedByUserId(createdByUserId);
        dto.setDescription("Leaking pipe");
        dto.setPriority("HIGH");

        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Property not found: " + propertyId))
                .when(propertyServiceClient).validatePropertyExists(propertyId);

        assertThrows(ResourceNotFoundException.class,
                () -> maintenanceRequestService.createRequest(dto));
    }
}