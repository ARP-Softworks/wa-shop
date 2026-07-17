package uy.washop.product.application;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import uy.washop.product.domain.ProductImage;
import uy.washop.product.infrastructure.ProductImageRepository;

/**
 * Loads one primary image URL per product in a single query (main image first, else lowest position).
 */
@Component
public class PrimaryImageUrlLoader {

    private final ProductImageRepository productImageRepository;

    public PrimaryImageUrlLoader(ProductImageRepository productImageRepository) {
        this.productImageRepository = productImageRepository;
    }

    public Map<UUID, String> load(Collection<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        List<ProductImage> images = productImageRepository.findByProductIdInOrderedForPrimary(productIds);
        Map<UUID, String> primaryByProduct = new LinkedHashMap<>();
        for (ProductImage image : images) {
            primaryByProduct.putIfAbsent(image.getProduct().getId(), image.getUrl());
        }
        return primaryByProduct;
    }
}
