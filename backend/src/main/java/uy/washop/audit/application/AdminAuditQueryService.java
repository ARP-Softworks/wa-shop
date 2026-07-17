package uy.washop.audit.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uy.washop.audit.api.dto.AuditLogResponse;
import uy.washop.audit.api.mapper.AuditLogMapper;
import uy.washop.audit.infrastructure.AuditLogRepository;
import uy.washop.shared.api.PageResponse;

@Service
public class AdminAuditQueryService {

    private final AuditLogRepository auditLogRepository;

    public AdminAuditQueryService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> list(int page, int size) {
        Page<uy.washop.audit.domain.AuditLog> result = auditLogRepository.findAll(
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(Math.max(size, 1), 100),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );
        return new PageResponse<>(
                result.getContent().stream().map(AuditLogMapper::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
