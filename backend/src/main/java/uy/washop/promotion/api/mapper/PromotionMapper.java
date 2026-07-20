package uy.washop.promotion.api.mapper;

import uy.washop.promotion.api.dto.PromotionResponse;
import uy.washop.promotion.api.dto.PublicPromotionResponse;
import uy.washop.promotion.domain.Promotion;

public final class PromotionMapper {

    private PromotionMapper() {
    }

    public static PromotionResponse toResponse(Promotion promotion) {
        return new PromotionResponse(
                promotion.getId(),
                promotion.getName(),
                promotion.isActive(),
                promotion.getTriggerCategoryId(),
                promotion.getTriggerCategory().getName(),
                promotion.getTriggerQuantity(),
                promotion.getRewardCategoryId(),
                promotion.getRewardCategory().getName(),
                promotion.getRewardQuantity(),
                promotion.getDiscountPercent(),
                promotion.getCreatedAt(),
                promotion.getUpdatedAt()
        );
    }

    public static PublicPromotionResponse toPublicResponse(Promotion promotion) {
        return new PublicPromotionResponse(
                promotion.getTriggerCategoryId(),
                promotion.getTriggerQuantity(),
                promotion.getRewardCategoryId(),
                promotion.getRewardQuantity(),
                promotion.getDiscountPercent()
        );
    }
}
