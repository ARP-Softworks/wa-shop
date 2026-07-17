package uy.washop.audit.application;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uy.washop.audit.domain.AuditAction;
import uy.washop.audit.domain.AuditLog;
import uy.washop.audit.infrastructure.AuditLogRepository;
import uy.washop.security.AdminUserDetails;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(AuditAction action, String entityType, UUID entityId, String details) {
        AuditLog log = new AuditLog();
        log.setUserId(currentUserId());
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId != null ? entityId.toString() : null);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    public UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AdminUserDetails details) {
            return details.getUser().getId();
        }
        return null;
    }
}
