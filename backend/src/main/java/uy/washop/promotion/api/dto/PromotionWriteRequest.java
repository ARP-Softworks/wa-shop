package uy.washop.promotion.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record PromotionWriteRequest(
        @NotBlank @Size(max = 200) String name,
        boolean active,
        @NotNull UUID triggerCategoryId,
        @Min(1) int triggerQuantity,
        @NotNull UUID rewardCategoryId,
        @Min(1) int rewardQuantity,
        @Min(1) @Max(100) int discountPercent
) {
}
