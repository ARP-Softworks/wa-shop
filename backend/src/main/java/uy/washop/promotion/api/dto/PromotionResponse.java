package uy.washop.promotion.api.dto;

import java.time.Instant;
import java.util.UUID;

public record PromotionResponse(
        UUID id,
        String name,
        boolean active,
        UUID triggerCategoryId,
        String triggerCategoryName,
        int triggerQuantity,
        UUID rewardCategoryId,
        String rewardCategoryName,
        int rewardQuantity,
        int discountPercent,
        Instant createdAt,
        Instant updatedAt
) {
}
