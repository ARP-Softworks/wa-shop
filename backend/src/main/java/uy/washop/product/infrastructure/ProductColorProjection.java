package uy.washop.product.infrastructure;

import java.util.UUID;

/** Row projection for {@link ProductVariantRepository#findAvailableColorsByProductIds}. */
public interface ProductColorProjection {
    UUID getProductId();

    String getColor();
}
