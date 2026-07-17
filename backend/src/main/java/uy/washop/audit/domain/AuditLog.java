package uy.washop.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import uy.washop.shared.domain.BaseEntity;

@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    @Column(name = "user_id")
    private UUID userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AuditAction action;

    @NotBlank
    @Size(max = 80)
    @Column(name = "entity_type", nullable = false, length = 80)
    private String entityType;

    @Size(max = 64)
    @Column(name = "entity_id", length = 64)
    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
