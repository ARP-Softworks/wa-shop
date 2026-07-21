package uy.washop.audit.application;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uy.washop.audit.api.dto.AuditLogResponse;
import uy.washop.audit.api.mapper.AuditLogMapper;
import uy.washop.audit.domain.AuditLog;
import uy.washop.audit.infrastructure.AuditLogRepository;
import uy.washop.auth.domain.User;
import uy.washop.auth.infrastructure.UserRepository;
import uy.washop.shared.api.PageResponse;

@Service
public class AdminAuditQueryService {

    private static final String SYSTEM_ACTOR = "Sistema (automático)";

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AdminAuditQueryService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> list(int page, int size) {
        Page<AuditLog> result = auditLogRepository.findAll(
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(Math.max(size, 1), 100),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        List<UUID> userIds = result.getContent().stream()
                .map(AuditLog::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, String> actorNames = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, AdminAuditQueryService::displayName));

        return new PageResponse<>(
                result.getContent().stream()
                        .map(log -> AuditLogMapper.toResponse(
                                log,
                                log.getUserId() == null
                                        ? SYSTEM_ACTOR
                                        : actorNames.getOrDefault(log.getUserId(), "Usuario eliminado")
                        ))
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private static String displayName(User user) {
        String name = ((user.getFirstName() == null ? "" : user.getFirstName())
                + " " + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return name.isEmpty() ? user.getEmail() : name + " (" + user.getEmail() + ")";
    }
}
