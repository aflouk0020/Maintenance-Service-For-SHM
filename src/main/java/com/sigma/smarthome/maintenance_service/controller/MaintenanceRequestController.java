package com.sigma.smarthome.maintenance_service.controller;

import com.sigma.smarthome.maintenance_service.dto.AssignStaffDto;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import com.sigma.smarthome.maintenance_service.dto.CreateMaintenanceRequestDto;
import com.sigma.smarthome.maintenance_service.dto.MaintenanceRequestResponse;
import com.sigma.smarthome.maintenance_service.dto.UpdateMaintenanceStatusDto;
import com.sigma.smarthome.maintenance_service.entity.MaintenanceRequest;
import com.sigma.smarthome.maintenance_service.service.MaintenanceRequestService;
import jakarta.validation.Valid;
import com.sigma.smarthome.maintenance_service.dto.MaintenanceHistoryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import com.sigma.smarthome.maintenance_service.dto.EdgeHeartbeatDto;

@RestController
@RequestMapping("/api/v1/maintenance-requests")
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceRequestService;
    private static Map<String, Object> lastHeartbeat = new HashMap<>();
    
    public MaintenanceRequestController(MaintenanceRequestService maintenanceRequestService) {
        this.maintenanceRequestService = maintenanceRequestService;
    }

    @PostMapping
    public ResponseEntity<MaintenanceRequestResponse> createRequest(
            @Valid @RequestBody CreateMaintenanceRequestDto dto) {
        MaintenanceRequestResponse created = maintenanceRequestService.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasRole('PROPERTY_MANAGER')")
    public ResponseEntity<List<MaintenanceRequest>> getRequestsForManager(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID managerId = UUID.fromString(authentication.getName());

        List<MaintenanceRequest> requests =
                maintenanceRequestService.getRequestsForManager(managerId, authorizationHeader, status, priority);

        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('PROPERTY_MANAGER')")
    public ResponseEntity<MaintenanceRequest> assignStaff(
            @PathVariable UUID id,
            @Valid @RequestBody AssignStaffDto dto,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        MaintenanceRequest updated =
                maintenanceRequestService.assignStaff(id, dto.getStaffId(), authorizationHeader);

        return ResponseEntity.ok(updated);
    }
    
    @PostMapping("/edge/heartbeat")
    public ResponseEntity<Map<String, Object>> receiveEdgeHeartbeat(
            @Valid @RequestBody EdgeHeartbeatDto dto
    ) {
        // ✅ Store latest heartbeat
        lastHeartbeat = Map.of(
                "deviceId", dto.getDeviceId(),
                "propertyId", dto.getPropertyId(),
                "status", dto.getStatus(),
                "temperature", dto.getTemperature(),
                "humidity", dto.getHumidity(),
                "timestamp", dto.getTimestamp()
        );

        System.out.println("Edge heartbeat received: " + lastHeartbeat);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                Map.of(
                        "message", "Edge heartbeat received successfully",
                        "deviceId", dto.getDeviceId(),
                        "status", dto.getStatus()
                )
        );
    }
    
    @GetMapping("/edge/heartbeat/latest")
    public ResponseEntity<Map<String, Object>> getLatestHeartbeat() {
        if (lastHeartbeat == null || lastHeartbeat.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No heartbeat received yet"));
        }

        Map<String, Object> response = new HashMap<>(lastHeartbeat);

        try {
            Object timestampObj = lastHeartbeat.get("timestamp");

            if (timestampObj != null) {
                OffsetDateTime heartbeatTime = OffsetDateTime.parse(timestampObj.toString());
                long secondsSinceLastHeartbeat =
                        Duration.between(heartbeatTime, OffsetDateTime.now()).getSeconds();

                response.put("secondsSinceLastHeartbeat", secondsSinceLastHeartbeat);

                if (secondsSinceLastHeartbeat > 30) {
                    response.put("status", "OFFLINE");
                }
            }
        } catch (Exception ex) {
            response.put("status", "UNKNOWN");
            response.put("statusNote", "Unable to evaluate heartbeat freshness");
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('MAINTENANCE_STAFF')")
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

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceRequestResponse> getRequestById(@PathVariable UUID id) {
        MaintenanceRequestResponse response = maintenanceRequestService.getRequestById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/history")
    public ResponseEntity<List<MaintenanceHistoryResponse>> getRequestHistory(@PathVariable UUID id) {
        List<MaintenanceHistoryResponse> history = maintenanceRequestService.getRequestHistory(id);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/assigned")
    @PreAuthorize("hasRole('MAINTENANCE_STAFF')")
    public ResponseEntity<List<MaintenanceRequest>> getRequestsForStaff() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID staffId = UUID.fromString(authentication.getName());

        List<MaintenanceRequest> requests = maintenanceRequestService.getRequestsForStaff(staffId);
        return ResponseEntity.ok(requests);
    }
}