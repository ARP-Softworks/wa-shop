package uy.washop.discount.api.mapper;

import uy.washop.discount.api.dto.DiscountCodeResponse;
import uy.washop.discount.domain.DiscountCode;

public final class DiscountCodeMapper {

    private DiscountCodeMapper() {
    }

    /**
     * usedCount is passed in rather than read from the entity: the stored column is never
     * incremented (enforcement counts DiscountCodeRedemption rows instead), so it would always
     * report zero. Callers must compute the real count from DiscountCodeRedemptionRepository.
     */
    public static DiscountCodeResponse toResponse(DiscountCode entity, long usedCount) {
        return new DiscountCodeResponse(
                entity.getId(),
                entity.getCode(),
                entity.isActive(),
                entity.getDiscountType(),
                entity.getDiscountValue(),
                entity.getMaxUses(),
                usedCount,
                entity.getStartsAt(),
                entity.getEndsAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
