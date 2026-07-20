package uy.washop.product.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uy.washop.product.domain.ProductCondition;
import uy.washop.shared.domain.CurrencyCode;

public record ProductVariantAdminResponse(
        UUID id,
        ProductCondition condition,
        String storageCapacity,
        String color,
        Integer batteryHealth,
        BigDecimal price,
        BigDecimal previousPrice,
        CurrencyCode currency,
        int stock,
        String warranty,
        String imei,
        boolean published,
        List<ProductImageResponse> images,
        Instant createdAt,
        Instant updatedAt
) {
}
