package uy.washop.discount.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import uy.washop.discount.domain.DiscountType;

public record DiscountCodeResponse(
        UUID id,
        String code,
        boolean active,
        DiscountType discountType,
        BigDecimal discountValue,
        Integer maxUses,
        long usedCount,
        Instant startsAt,
        Instant endsAt,
        Instant createdAt,
        Instant updatedAt
) {
}
