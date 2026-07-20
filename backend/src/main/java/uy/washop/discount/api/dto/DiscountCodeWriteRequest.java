package uy.washop.discount.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import uy.washop.discount.domain.DiscountType;

public record DiscountCodeWriteRequest(
        @NotBlank @Size(max = 40) String code,
        boolean active,
        @NotNull DiscountType discountType,
        @NotNull @DecimalMin("0.01") BigDecimal discountValue,
        Integer maxUses,
        Instant startsAt,
        Instant endsAt
) {
}
