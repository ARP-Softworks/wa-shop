package uy.washop.category.api.dto;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        boolean active,
        String seoTitle,
        String metaDescription,
        boolean indexable,
        Instant createdAt,
        Instant updatedAt
) {
}
