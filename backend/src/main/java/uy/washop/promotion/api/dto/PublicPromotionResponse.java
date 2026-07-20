package uy.washop.promotion.api.dto;

import java.util.UUID;

/** Minimal shape the storefront needs to preview the discount client-side before checkout. */
public record PublicPromotionResponse(
        UUID triggerCategoryId,
        int triggerQuantity,
        UUID rewardCategoryId,
        int rewardQuantity,
        int discountPercent
) {
}
