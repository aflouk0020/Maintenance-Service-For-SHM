package com.sigma.smarthome.maintenance_service.repository;

import com.sigma.smarthome.maintenance_service.entity.MaintenanceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false"
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Sql(
    statements = {
        "DROP TABLE IF EXISTS maintenance_requests",
        "CREATE TABLE maintenance_requests (" +
        "id UUID PRIMARY KEY, " +
        "property_id UUID NOT NULL, " +
        "created_by_user_id UUID NOT NULL, " +
        "assigned_staff_id UUID, " +
        "description TEXT NOT NULL, " +
        "priority VARCHAR(20) NOT NULL, " +
        "status VARCHAR(30) NOT NULL, " +
        "created_at TIMESTAMP NOT NULL, " +
        "updated_at TIMESTAMP NOT NULL, " +
        "completed_at TIMESTAMP" +
        ")"
    },
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD
)
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