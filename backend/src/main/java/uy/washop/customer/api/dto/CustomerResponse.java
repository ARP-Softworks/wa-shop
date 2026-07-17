package uy.washop.customer.api.dto;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String phone,
        String email,
        String address,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
