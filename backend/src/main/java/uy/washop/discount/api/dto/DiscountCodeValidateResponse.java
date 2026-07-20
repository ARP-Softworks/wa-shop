package uy.washop.discount.api.dto;

import java.math.BigDecimal;
import uy.washop.discount.domain.DiscountType;

public record DiscountCodeValidateResponse(
        String code,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal discountAmount,
        BigDecimal eligibleSubtotal
) {
}
