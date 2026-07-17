package uy.washop.product.domain;

import java.util.List;
import uy.washop.shared.exception.BusinessConflictException;

public final class ProductImageRules {

    private ProductImageRules() {
    }

    public static void validateSingleMainImage(List<ProductImage> images) {
        long mainCount = images.stream().filter(ProductImage::isMainImage).count();
        if (mainCount > 1) {
            throw new BusinessConflictException("Solo puede haber una imagen principal por producto");
        }
    }
}
