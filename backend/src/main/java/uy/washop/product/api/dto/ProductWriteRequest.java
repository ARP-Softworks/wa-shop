package uy.washop.product.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.shared.domain.CurrencyCode;

public record ProductWriteRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 220) String slug,
        @Size(max = 120) String model,
        String description,
        @NotNull ProductType productType,
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
        boolean featured,
        UUID categoryId,
        @Size(max = 70) String seoTitle,
        @Size(max = 320) String metaDescription,
        Boolean indexable,
        @Valid List<ProductFeatureWriteRequest> features,
        @Valid List<ProductImageWriteRequest> images
) {
}
