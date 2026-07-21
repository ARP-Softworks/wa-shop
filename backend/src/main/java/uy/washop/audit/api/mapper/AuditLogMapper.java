package uy.washop.audit.api.mapper;

import uy.washop.audit.api.dto.AuditLogResponse;
import uy.washop.audit.domain.AuditLog;

public final class AuditLogMapper {

    private AuditLogMapper() {
    }

    public static AuditLogResponse toResponse(AuditLog log, String actorName) {
        return new AuditLogResponse(
                log.getId(),
                log.getUserId(),
                actorName,
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }
}
