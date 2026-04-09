	package com.sigma.smarthome.maintenance_service.entity;
	
	import jakarta.persistence.*;
	import java.time.LocalDateTime;
	import java.util.UUID;
	
	@Entity
	@Table(name = "maintenance_history")
	public class MaintenanceHistory {
	
	    @Id
	    @GeneratedValue
	    private UUID id;
	
	    @Column(name = "request_id", nullable = false)
	    private UUID requestId;
	
	    @Column(name = "old_status", nullable = false, length = 30)
	    private String oldStatus;
	
	    @Column(name = "new_status", nullable = false, length = 30)
	    private String newStatus;
	
	    @Column(name = "changed_by_user_id", nullable = false)
	    private UUID changedByUserId;
	
	    @Column(name = "changed_at", nullable = false)
	    private LocalDateTime changedAt;
	
	    @PrePersist
	    public void prePersist() {
	        if (changedAt == null) {
	            changedAt = LocalDateTime.now();
	        }
	    }
	
	    public UUID getId() {
	        return id;
	    }
	
	    public void setId(UUID id) {
	        this.id = id;
	    }
	
	    public UUID getRequestId() {
	        return requestId;
	    }
	
	    public void setRequestId(UUID requestId) {
	        this.requestId = requestId;
	    }
	
	    public String getOldStatus() {
	        return oldStatus;
	    }
	
	    public void setOldStatus(String oldStatus) {
	        this.oldStatus = oldStatus;
	    }
	
	    public String getNewStatus() {
	        return newStatus;
	    }
	
	    public void setNewStatus(String newStatus) {
	        this.newStatus = newStatus;
	    }
	
	    public UUID getChangedByUserId() {
	        return changedByUserId;
	    }
	
	    public void setChangedByUserId(UUID changedByUserId) {
	        this.changedByUserId = changedByUserId;
	    }
	
	    public LocalDateTime getChangedAt() {
	        return changedAt;
	    }
	
	    public void setChangedAt(LocalDateTime changedAt) {
	        this.changedAt = changedAt;
	    }
	}