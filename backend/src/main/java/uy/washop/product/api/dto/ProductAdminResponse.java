package uy.washop.product.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.shared.domain.CurrencyCode;

/** Admin projection — includes IMEI. Never use for public endpoints. */
public record ProductAdminResponse(
        UUID id,
        String slug,
        String name,
        String model,
        String description,
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
        String imei,
        boolean published,
        boolean featured,
        UUID categoryId,
        String categoryName,
        List<ProductImageResponse> images,
        List<ProductFeatureResponse> features,
        String seoTitle,
        String metaDescription,
        boolean indexable,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
