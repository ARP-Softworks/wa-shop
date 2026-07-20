package uy.washop.discount.api.mapper;

import uy.washop.discount.api.dto.DiscountCodeResponse;
import uy.washop.discount.domain.DiscountCode;

public final class DiscountCodeMapper {

    private DiscountCodeMapper() {
    }

    public static DiscountCodeResponse toResponse(DiscountCode entity) {
        return new DiscountCodeResponse(
                entity.getId(),
                entity.getCode(),
                entity.isActive(),
                entity.getDiscountType(),
                entity.getDiscountValue(),
                entity.getMaxUses(),
                entity.getUsedCount(),
                entity.getStartsAt(),
                entity.getEndsAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
