package uy.washop.auth.api.dto;

import java.util.UUID;
import uy.washop.auth.domain.UserRole;

public record AuthenticatedUserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        UserRole role
) {
}
