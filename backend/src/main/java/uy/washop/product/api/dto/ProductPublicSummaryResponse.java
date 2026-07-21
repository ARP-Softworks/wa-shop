package uy.washop.product.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.shared.domain.CurrencyCode;

/** Lightweight public list item — never includes IMEI. */
public record ProductPublicSummaryResponse(
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
        BigDecimal previousPrice,
        Integer promoBuyQuantity,
        Integer promoPayQuantity,
        CurrencyCode currency,
        int stock,
        String warranty,
        boolean featured,
        String primaryImageUrl,
        Instant createdAt,
        UUID categoryId,
        List<String> availableColors
) {
}
