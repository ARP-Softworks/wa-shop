package uy.washop.product.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import uy.washop.product.domain.ProductCondition;
import uy.washop.shared.domain.CurrencyCode;

public record ProductVariantPublicResponse(
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
        boolean published,
        String primaryImageUrl,
        List<ProductPublicImageResponse> images
) {
}
