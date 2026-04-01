package com.sigma.smarthome.maintenance_service.repository;

import com.sigma.smarthome.maintenance_service.entity.MaintenanceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class MaintenanceRequestRepositoryTest {

    @Autowired
    private MaintenanceRequestRepository maintenanceRequestRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void findByPropertyIdIn_ShouldReturnMatchingRequests() {
        UUID propertyId1 = UUID.randomUUID();
        UUID propertyId2 = UUID.randomUUID();
        UUID otherPropertyId = UUID.randomUUID();

        MaintenanceRequest req1 = new MaintenanceRequest();
        req1.setPropertyId(propertyId1);
        req1.setCreatedByUserId(UUID.randomUUID());
        req1.setAssignedStaffId(UUID.randomUUID());
        req1.setDescription("Leaking pipe");
        req1.setPriority("HIGH");
        req1.setStatus("OPEN");
        testEntityManager.persist(req1);

        MaintenanceRequest req2 = new MaintenanceRequest();
        req2.setPropertyId(propertyId2);
        req2.setCreatedByUserId(UUID.randomUUID());
        req2.setAssignedStaffId(UUID.randomUUID());
        req2.setDescription("Broken heater");
        req2.setPriority("MEDIUM");
        req2.setStatus("IN_PROGRESS");
        testEntityManager.persist(req2);

        MaintenanceRequest req3 = new MaintenanceRequest();
        req3.setPropertyId(otherPropertyId);
        req3.setCreatedByUserId(UUID.randomUUID());
        req3.setAssignedStaffId(UUID.randomUUID());
        req3.setDescription("Door issue");
        req3.setPriority("LOW");
        req3.setStatus("OPEN");
        testEntityManager.persist(req3);

        testEntityManager.flush();

        List<MaintenanceRequest> result =
                maintenanceRequestRepository.findByPropertyIdIn(List.of(propertyId1, propertyId2));

        assertEquals(2, result.size());
    }

    @Test
    void findByPropertyIdIn_ShouldReturnEmptyList_WhenNoPropertyIdsMatch() {
        UUID propertyId = UUID.randomUUID();

        MaintenanceRequest req = new MaintenanceRequest();
        req.setPropertyId(propertyId);
        req.setCreatedByUserId(UUID.randomUUID());
        req.setAssignedStaffId(UUID.randomUUID());
        req.setDescription("Window issue");
        req.setPriority("LOW");
        req.setStatus("OPEN");
        testEntityManager.persist(req);

        testEntityManager.flush();

        List<MaintenanceRequest> result =
                maintenanceRequestRepository.findByPropertyIdIn(List.of(UUID.randomUUID()));

        assertTrue(result.isEmpty());
    }
}