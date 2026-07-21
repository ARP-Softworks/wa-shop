package uy.washop.audit.api.dto;

import java.time.Instant;
import java.util.UUID;
import uy.washop.audit.domain.AuditAction;

public record AuditLogResponse(
        UUID id,
        UUID userId,
        String actorName,
        AuditAction action,
        String entityType,
        String entityId,
        String details,
        Instant createdAt
) {
}
