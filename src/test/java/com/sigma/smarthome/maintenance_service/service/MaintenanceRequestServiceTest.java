package com.sigma.smarthome.maintenance_service.service;

import com.sigma.smarthome.maintenance_service.client.PropertyServiceClient;
import com.sigma.smarthome.maintenance_service.dto.CreateMaintenanceRequestDto;
import com.sigma.smarthome.maintenance_service.dto.MaintenanceRequestResponse;
import com.sigma.smarthome.maintenance_service.entity.MaintenanceRequest;
import com.sigma.smarthome.maintenance_service.exception.ForbiddenOperationException;
import com.sigma.smarthome.maintenance_service.exception.ResourceNotFoundException;
import com.sigma.smarthome.maintenance_service.repository.MaintenanceRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private UUID managerId;

    @BeforeEach
    void setUp() {
        propertyId = UUID.randomUUID();
        createdByUserId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        managerId = UUID.randomUUID();
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

    @Test
    void getRequestsForManager_ShouldReturnRequests_WhenManagerHasProperties() {
        UUID propertyId1 = UUID.randomUUID();
        UUID propertyId2 = UUID.randomUUID();

        MaintenanceRequest request1 = new MaintenanceRequest();
        request1.setId(UUID.randomUUID());
        request1.setPropertyId(propertyId1);
        request1.setStatus("OPEN");

        MaintenanceRequest request2 = new MaintenanceRequest();
        request2.setId(UUID.randomUUID());
        request2.setPropertyId(propertyId2);
        request2.setStatus("IN_PROGRESS");

        List<UUID> propertyIds = List.of(propertyId1, propertyId2);
        List<MaintenanceRequest> requests = List.of(request1, request2);

        when(propertyServiceClient.getPropertyIdsManagedBy(managerId, "Bearer test-token"))
                .thenReturn(propertyIds);

        when(maintenanceRequestRepository.findByPropertyIdIn(propertyIds))
                .thenReturn(requests);

        List<MaintenanceRequest> result =
                maintenanceRequestService.getRequestsForManager(managerId, "Bearer test-token", null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(propertyId1, result.get(0).getPropertyId());
        assertEquals(propertyId2, result.get(1).getPropertyId());

        verify(propertyServiceClient).getPropertyIdsManagedBy(managerId, "Bearer test-token");
        verify(maintenanceRequestRepository).findByPropertyIdIn(propertyIds);
    }

    @Test
    void getRequestsForManager_ShouldReturnEmptyList_WhenManagerHasNoProperties() {
        when(propertyServiceClient.getPropertyIdsManagedBy(managerId, "Bearer test-token"))
                .thenReturn(List.of());

        List<MaintenanceRequest> result =
                maintenanceRequestService.getRequestsForManager(managerId, "Bearer test-token", null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(propertyServiceClient).getPropertyIdsManagedBy(managerId, "Bearer test-token");
        verify(maintenanceRequestRepository, org.mockito.Mockito.never()).findByPropertyIdIn(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void getRequestsForManager_ShouldReturnEmptyList_WhenNoRequestsExistForManagedProperties() {
        List<UUID> propertyIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        when(propertyServiceClient.getPropertyIdsManagedBy(managerId, "Bearer test-token"))
                .thenReturn(propertyIds);

        when(maintenanceRequestRepository.findByPropertyIdIn(propertyIds))
                .thenReturn(List.of());

        List<MaintenanceRequest> result =
                maintenanceRequestService.getRequestsForManager(managerId, "Bearer test-token", null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(propertyServiceClient).getPropertyIdsManagedBy(managerId, "Bearer test-token");
        verify(maintenanceRequestRepository).findByPropertyIdIn(propertyIds);
    }

    @Test
    void getRequestsForManager_ShouldFilterByStatus_WhenStatusProvided() {
        UUID propertyId1 = UUID.randomUUID();

        MaintenanceRequest request1 = new MaintenanceRequest();
        request1.setId(UUID.randomUUID());
        request1.setPropertyId(propertyId1);
        request1.setStatus("OPEN");
        request1.setPriority("HIGH");

        List<UUID> propertyIds = List.of(propertyId1);

        when(propertyServiceClient.getPropertyIdsManagedBy(managerId, "Bearer test-token"))
                .thenReturn(propertyIds);

        when(maintenanceRequestRepository.findByPropertyIdInAndStatus(propertyIds, "OPEN"))
                .thenReturn(List.of(request1));

        List<MaintenanceRequest> result =
                maintenanceRequestService.getRequestsForManager(
                        managerId, "Bearer test-token", "OPEN", null
                );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("OPEN", result.get(0).getStatus());

        verify(propertyServiceClient).getPropertyIdsManagedBy(managerId, "Bearer test-token");
        verify(maintenanceRequestRepository).findByPropertyIdInAndStatus(propertyIds, "OPEN");
    }

    @Test
    void getRequestById_ShouldReturnResponse_WhenRequestExists() {
        MaintenanceRequest request = new MaintenanceRequest();
        request.setId(requestId);
        request.setPropertyId(propertyId);
        request.setCreatedByUserId(createdByUserId);
        request.setDescription("Broken boiler");
        request.setPriority("HIGH");
        request.setStatus("OPEN");

        when(maintenanceRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        MaintenanceRequestResponse result = maintenanceRequestService.getRequestById(requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals("OPEN", result.getStatus());
        verify(maintenanceRequestRepository).findById(requestId);
    }

    @Test
    void getRequestById_ShouldThrowNotFound_WhenRequestDoesNotExist() {
        when(maintenanceRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> maintenanceRequestService.getRequestById(requestId)
        );

        assertEquals("Maintenance request not found: " + requestId, ex.getMessage());
    }
    
    @Test
    void getRequestsForManager_ShouldFilterByPriority_WhenPriorityProvided() {
        UUID propertyId1 = UUID.randomUUID();

        MaintenanceRequest request1 = new MaintenanceRequest();
        request1.setId(UUID.randomUUID());
        request1.setPropertyId(propertyId1);
        request1.setStatus("OPEN");
        request1.setPriority("HIGH");

        List<UUID> propertyIds = List.of(propertyId1);

        when(propertyServiceClient.getPropertyIdsManagedBy(managerId, "Bearer test-token"))
                .thenReturn(propertyIds);

        when(maintenanceRequestRepository.findByPropertyIdInAndPriority(propertyIds, "HIGH"))
                .thenReturn(List.of(request1));

        List<MaintenanceRequest> result =
                maintenanceRequestService.getRequestsForManager(
                        managerId, "Bearer test-token", null, "HIGH"
                );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("HIGH", result.get(0).getPriority());

        verify(propertyServiceClient).getPropertyIdsManagedBy(managerId, "Bearer test-token");
        verify(maintenanceRequestRepository).findByPropertyIdInAndPriority(propertyIds, "HIGH");
    }

    @Test
    void getRequestsForManager_ShouldFilterByStatusAndPriority_WhenBothProvided() {
        UUID propertyId1 = UUID.randomUUID();

        MaintenanceRequest request1 = new MaintenanceRequest();
        request1.setId(UUID.randomUUID());
        request1.setPropertyId(propertyId1);
        request1.setStatus("OPEN");
        request1.setPriority("HIGH");

        List<UUID> propertyIds = List.of(propertyId1);

        when(propertyServiceClient.getPropertyIdsManagedBy(managerId, "Bearer test-token"))
                .thenReturn(propertyIds);

        when(maintenanceRequestRepository.findByPropertyIdInAndStatusAndPriority(propertyIds, "OPEN", "HIGH"))
                .thenReturn(List.of(request1));

        List<MaintenanceRequest> result =
                maintenanceRequestService.getRequestsForManager(
                        managerId, "Bearer test-token", "OPEN", "HIGH"
                );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("OPEN", result.get(0).getStatus());
        assertEquals("HIGH", result.get(0).getPriority());

        verify(propertyServiceClient).getPropertyIdsManagedBy(managerId, "Bearer test-token");
        verify(maintenanceRequestRepository).findByPropertyIdInAndStatusAndPriority(propertyIds, "OPEN", "HIGH");
    }
}