package uy.washop.audit.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uy.washop.audit.domain.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
