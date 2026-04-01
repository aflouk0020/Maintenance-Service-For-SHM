package com.sigma.smarthome.maintenance_service.controller;

import com.sigma.smarthome.maintenance_service.entity.MaintenanceRequest;
import com.sigma.smarthome.maintenance_service.exception.ForbiddenOperationException;
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

class MaintenanceRequestControllerManagerOverviewTest {

    private MaintenanceRequestService maintenanceRequestService;
    private MaintenanceRequestController maintenanceRequestController;

    @BeforeEach
    void setUp() {
        maintenanceRequestService = Mockito.mock(MaintenanceRequestService.class);
        maintenanceRequestController = new MaintenanceRequestController(maintenanceRequestService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void getRequestsForManager_ShouldReturn200_WhenManagerRequestsOverview() {
        UUID managerId = UUID.randomUUID();

        MaintenanceRequest request1 = new MaintenanceRequest();
        request1.setId(UUID.randomUUID());
        request1.setPropertyId(UUID.randomUUID());
        request1.setStatus("OPEN");

        MaintenanceRequest request2 = new MaintenanceRequest();
        request2.setId(UUID.randomUUID());
        request2.setPropertyId(UUID.randomUUID());
        request2.setStatus("IN_PROGRESS");

        Mockito.when(maintenanceRequestService.getRequestsForManager(managerId, "Bearer test-token"))
                .thenReturn(List.of(request1, request2));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        managerId.toString(),
                        "test-token",
                        List.of()
                )
        );

        ResponseEntity<List<MaintenanceRequest>> response =
                maintenanceRequestController.getRequestsForManager("Bearer test-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("OPEN", response.getBody().get(0).getStatus());
        assertEquals("IN_PROGRESS", response.getBody().get(1).getStatus());
    }

    @Test
    void getRequestsForManager_ShouldReturn200_WithEmptyList_WhenNoRequestsExist() {
        UUID managerId = UUID.randomUUID();

        Mockito.when(maintenanceRequestService.getRequestsForManager(managerId, "Bearer test-token"))
                .thenReturn(List.of());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        managerId.toString(),
                        "test-token",
                        List.of()
                )
        );

        ResponseEntity<List<MaintenanceRequest>> response =
                maintenanceRequestController.getRequestsForManager("Bearer test-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void getRequestsForManager_ShouldThrow_WhenAuthenticationMissing() {
        SecurityContextHolder.clearContext();

        assertThrows(NullPointerException.class, () ->
        		maintenanceRequestController.getRequestsForManager("Bearer test-token")
        );
    }

    @Test
    void managerShouldNotBeAbleToUpdateMaintenanceRequest() {
        UUID managerId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        managerId.toString(),
                        "test-token",
                        List.of()
                )
        );

        Mockito.when(maintenanceRequestService.updateStatus(requestId, managerId, "IN_PROGRESS"))
                .thenThrow(new ForbiddenOperationException("Access Denied"));

        ForbiddenOperationException ex = assertThrows(
                ForbiddenOperationException.class,
                () -> maintenanceRequestController.updateStatus(
                        requestId,
                        new com.sigma.smarthome.maintenance_service.dto.UpdateMaintenanceStatusDto() {{
                            setStatus("IN_PROGRESS");
                        }}
                )
        );

        assertEquals("Access Denied", ex.getMessage());
    }
}