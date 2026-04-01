package com.sigma.smarthome.maintenance_service.controller;

import com.sigma.smarthome.maintenance_service.dto.CreateMaintenanceRequestDto;
import com.sigma.smarthome.maintenance_service.dto.UpdateMaintenanceStatusDto;
import com.sigma.smarthome.maintenance_service.entity.MaintenanceRequest;
import com.sigma.smarthome.maintenance_service.service.MaintenanceRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maintenance-requests")
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceRequestService;

    public MaintenanceRequestController(MaintenanceRequestService maintenanceRequestService) {
        this.maintenanceRequestService = maintenanceRequestService;
    }

    @PostMapping
    public ResponseEntity<MaintenanceRequest> createRequest(
            @Valid @RequestBody CreateMaintenanceRequestDto dto) {
        MaintenanceRequest created = maintenanceRequestService.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('MAINTENANCE_STAFF')")
    @PutMapping("/{id}/status")
    public ResponseEntity<MaintenanceRequest> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMaintenanceStatusDto dto
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID loggedInUserId = UUID.fromString(authentication.getName());

        MaintenanceRequest updated = maintenanceRequestService.updateStatus(
                id,
                loggedInUserId,
                dto.getStatus()
        );

        return ResponseEntity.ok(updated);
    }
}