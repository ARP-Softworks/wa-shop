package uy.washop.auth.api.dto;

import java.time.Instant;
import java.util.UUID;
import uy.washop.auth.domain.UserRole;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
