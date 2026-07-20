package uy.washop.product.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import uy.washop.product.domain.ProductCondition;
import uy.washop.shared.domain.CurrencyCode;

public record ProductVariantWriteRequest(
        UUID id,
        @NotNull ProductCondition condition,
        @Size(max = 40) String storageCapacity,
        @Size(max = 80) String color,
        @Min(0) @Max(100) Integer batteryHealth,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @DecimalMin("0.00") BigDecimal previousPrice,
        @NotNull CurrencyCode currency,
        @Min(0) int stock,
        @Size(max = 200) String warranty,
        @Size(min = 14, max = 17) String imei,
        boolean published,
        @Valid List<ProductImageWriteRequest> images
) {
}
