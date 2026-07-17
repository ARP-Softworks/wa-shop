package uy.washop.technicalservice.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import uy.washop.shared.domain.CurrencyCode;

public record TechnicalServiceResponse(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        CurrencyCode currency,
        String estimatedTime,
        boolean active,
        String seoTitle,
        String metaDescription,
        boolean indexable,
        Instant createdAt,
        Instant updatedAt
) {
}
