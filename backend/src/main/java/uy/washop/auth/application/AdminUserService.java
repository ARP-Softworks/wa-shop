package uy.washop.auth.application;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.auth.api.dto.UserResponse;
import uy.washop.auth.api.dto.UserWriteRequest;
import uy.washop.auth.api.mapper.UserMapper;
import uy.washop.auth.domain.User;
import uy.washop.auth.domain.UserRole;
import uy.washop.auth.infrastructure.UserRepository;
import uy.washop.security.AdminUserDetails;
import uy.washop.shared.api.PageResponse;
import uy.washop.shared.exception.BusinessConflictException;
import uy.washop.shared.exception.ResourceNotFoundException;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> search(String q, int page, int size) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 48),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        String term = StringUtils.hasText(q) ? q.trim() : "";
        Page<User> result = userRepository
                .findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        term, term, term, pageable
                );
        return new PageResponse<>(
                result.getContent().stream().map(UserMapper::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return UserMapper.toResponse(require(id));
    }

    @Transactional
    public UserResponse create(UserWriteRequest request) {
        String email = request.email().trim();
        validateEmail(email, null);
        if (!StringUtils.hasText(request.password())) {
            throw new BusinessConflictException("La contraseña es obligatoria");
        }

        User user = new User();
        user.setEmail(email);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setRole(UserRole.ADMIN);
        user.setEnabled(request.enabled());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        auditService.record(AuditAction.CREATE, "User", user.getId(), "Usuario creado: " + user.getEmail());
        return UserMapper.toResponse(user);
    }

    @Transactional
    public UserResponse update(UUID id, UserWriteRequest request) {
        User user = require(id);
        String email = request.email().trim();
        validateEmail(email, id);

        if (!request.enabled() && user.isEnabled()) {
            if (id.equals(currentUserId())) {
                throw new BusinessConflictException("No podés deshabilitar tu propia cuenta");
            }
            if (userRepository.countByEnabledTrue() <= 1) {
                throw new BusinessConflictException("Debe existir al menos un administrador habilitado");
            }
        }

        user.setEmail(email);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEnabled(request.enabled());
        if (StringUtils.hasText(request.password())) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        user = userRepository.save(user);

        auditService.record(AuditAction.UPDATE, "User", user.getId(), "Usuario actualizado: " + user.getEmail());
        return UserMapper.toResponse(user);
    }

    private void validateEmail(String email, UUID currentId) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BusinessConflictException("Ya existe un usuario con ese email");
            }
        });
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AdminUserDetails details) {
            return details.getUser().getId();
        }
        return null;
    }

    private User require(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
