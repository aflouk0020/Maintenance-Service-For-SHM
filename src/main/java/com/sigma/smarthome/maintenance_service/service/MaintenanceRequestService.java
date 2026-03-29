package com.sigma.smarthome.maintenance_service.service;

import com.sigma.smarthome.maintenance_service.client.PropertyServiceClient;
import com.sigma.smarthome.maintenance_service.dto.CreateMaintenanceRequestDto;
import com.sigma.smarthome.maintenance_service.entity.MaintenanceRequest;
import com.sigma.smarthome.maintenance_service.repository.MaintenanceRequestRepository;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final PropertyServiceClient propertyServiceClient;

    public MaintenanceRequestService(MaintenanceRequestRepository maintenanceRequestRepository,
                                     PropertyServiceClient propertyServiceClient) {
        this.maintenanceRequestRepository = maintenanceRequestRepository;
        this.propertyServiceClient = propertyServiceClient;
    }

    public MaintenanceRequest createRequest(CreateMaintenanceRequestDto dto) {
        propertyServiceClient.validatePropertyExists(dto.getPropertyId());

        MaintenanceRequest request = new MaintenanceRequest();
        request.setPropertyId(dto.getPropertyId());
        request.setCreatedByUserId(dto.getCreatedByUserId());
        request.setDescription(dto.getDescription());
        request.setPriority(dto.getPriority());
        request.setStatus("OPEN");

        return maintenanceRequestRepository.save(request);
    }
}