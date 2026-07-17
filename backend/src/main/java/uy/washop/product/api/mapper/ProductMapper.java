package uy.washop.product.api.mapper;

import java.util.List;
import uy.washop.product.api.dto.AdminProductSummaryResponse;
import uy.washop.product.api.dto.ProductAdminResponse;
import uy.washop.product.api.dto.ProductFeatureResponse;
import uy.washop.product.api.dto.ProductImageResponse;
import uy.washop.product.api.dto.ProductPublicImageResponse;
import uy.washop.product.api.dto.ProductPublicResponse;
import uy.washop.product.api.dto.ProductPublicSummaryResponse;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductFeature;
import uy.washop.product.domain.ProductImage;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static AdminProductSummaryResponse toAdminSummary(Product product, String primaryImageUrl) {
        return new AdminProductSummaryResponse(
                product.getId(),
                product.getSlug(),
                product.getName(),
                product.getModel(),
                product.getProductType(),
                product.getCondition(),
                product.getStorageCapacity(),
                product.getColor(),
                product.getBatteryHealth(),
                product.getPrice(),
                product.getCurrency(),
                product.getStock(),
                product.getImei(),
                product.isPublished(),
                product.isFeatured(),
                primaryImageUrl,
                product.getUpdatedAt()
        );
    }

    public static ProductPublicSummaryResponse toPublicSummary(Product product, String primaryImageUrl) {
        return new ProductPublicSummaryResponse(
                product.getId(),
                product.getSlug(),
                product.getName(),
                product.getModel(),
                product.getProductType(),
                product.getCondition(),
                product.getStorageCapacity(),
                product.getColor(),
                product.getBatteryHealth(),
                product.getPrice(),
                product.getPreviousPrice(),
                product.getCurrency(),
                product.getStock(),
                product.getWarranty(),
                product.isFeatured(),
                primaryImageUrl,
                product.getCreatedAt()
        );
    }

    public static ProductPublicResponse toPublicResponse(
            Product product,
            List<ProductImage> images,
            List<ProductFeature> features
    ) {
        List<ProductPublicImageResponse> imageResponses =
                images.stream().map(ProductMapper::toPublicImageResponse).toList();
        String primaryImageUrl = imageResponses.stream()
                .filter(ProductPublicImageResponse::mainImage)
                .map(ProductPublicImageResponse::url)
                .findFirst()
                .orElse(imageResponses.isEmpty() ? null : imageResponses.getFirst().url());

        return new ProductPublicResponse(
                product.getId(),
                product.getSlug(),
                product.getName(),
                product.getModel(),
                product.getDescription(),
                product.getProductType(),
                product.getCondition(),
                product.getStorageCapacity(),
                product.getColor(),
                product.getBatteryHealth(),
                product.getPrice(),
                product.getPreviousPrice(),
                product.getCurrency(),
                product.getStock(),
                product.getWarranty(),
                product.isPublished(),
                product.isFeatured(),
                product.getCategoryId(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                primaryImageUrl,
                imageResponses,
                features.stream().map(ProductMapper::toFeatureResponse).toList(),
                product.getSeoTitle(),
                product.getMetaDescription(),
                product.isIndexable(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public static ProductAdminResponse toAdminResponse(
            Product product,
            List<ProductImage> images,
            List<ProductFeature> features
    ) {
        return new ProductAdminResponse(
                product.getId(),
                product.getSlug(),
                product.getName(),
                product.getModel(),
                product.getDescription(),
                product.getProductType(),
                product.getCondition(),
                product.getStorageCapacity(),
                product.getColor(),
                product.getBatteryHealth(),
                product.getPrice(),
                product.getPreviousPrice(),
                product.getCurrency(),
                product.getStock(),
                product.getWarranty(),
                product.getImei(),
                product.isPublished(),
                product.isFeatured(),
                product.getCategoryId(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                images.stream().map(ProductMapper::toImageResponse).toList(),
                features.stream().map(ProductMapper::toFeatureResponse).toList(),
                product.getSeoTitle(),
                product.getMetaDescription(),
                product.isIndexable(),
                product.getPublishedAt(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public static ProductImageResponse toImageResponse(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getUrl(),
                image.getPublicId(),
                image.getAltText(),
                image.getPosition(),
                image.isMainImage(),
                image.getFormat(),
                image.getSizeBytes(),
                image.getWidth(),
                image.getHeight()
        );
    }

    public static ProductPublicImageResponse toPublicImageResponse(ProductImage image) {
        return new ProductPublicImageResponse(
                image.getId(),
                image.getUrl(),
                image.getAltText(),
                image.getPosition(),
                image.isMainImage(),
                image.getWidth(),
                image.getHeight()
        );
    }

    public static ProductFeatureResponse toFeatureResponse(ProductFeature feature) {
        return new ProductFeatureResponse(feature.getId(), feature.getName(), feature.getValue());
    }
}
