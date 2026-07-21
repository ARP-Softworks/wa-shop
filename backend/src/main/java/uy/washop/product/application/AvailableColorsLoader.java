package uy.washop.product.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import uy.washop.product.infrastructure.ProductColorProjection;
import uy.washop.product.infrastructure.ProductVariantRepository;

/**
 * Loads the distinct in-stock, published variant colors per product in a single query
 * (used to render color swatches on catalog cards without an N+1 per card).
 */
@Component
public class AvailableColorsLoader {

    private final ProductVariantRepository productVariantRepository;

    public AvailableColorsLoader(ProductVariantRepository productVariantRepository) {
        this.productVariantRepository = productVariantRepository;
    }

    public Map<UUID, List<String>> load(Collection<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, List<String>> colorsByProduct = new LinkedHashMap<>();
        for (ProductColorProjection row : productVariantRepository.findAvailableColorsByProductIds(productIds)) {
            List<String> colors = colorsByProduct.computeIfAbsent(row.getProductId(), ignored -> new ArrayList<>());
            if (!colors.contains(row.getColor())) {
                colors.add(row.getColor());
            }
        }
        return colorsByProduct;
    }
}
