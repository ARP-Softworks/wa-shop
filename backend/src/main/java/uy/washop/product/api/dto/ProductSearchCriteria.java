package uy.washop.product.api.dto;

import java.math.BigDecimal;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;

public record ProductSearchCriteria(
        String q,
        ProductType productType,
        ProductCondition condition,
        String model,
        String storageCapacity,
        String color,
        Integer minBatteryHealth,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean featured,
        Boolean inStock
) {
}
