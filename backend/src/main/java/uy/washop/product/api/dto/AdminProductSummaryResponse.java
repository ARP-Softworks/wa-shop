package uy.washop.product.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.shared.domain.CurrencyCode;

public record AdminProductSummaryResponse(
        UUID id,
        String slug,
        String name,
        String model,
        ProductType productType,
        ProductCondition condition,
        String storageCapacity,
        String color,
        Integer batteryHealth,
        BigDecimal price,
        CurrencyCode currency,
        int stock,
        String imei,
        boolean published,
        boolean featured,
        String primaryImageUrl,
        Instant updatedAt
) {
}
