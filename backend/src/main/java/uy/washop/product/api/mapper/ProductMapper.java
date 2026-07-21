package uy.washop.product.api.mapper;

import java.util.List;
import java.util.Map;
import uy.washop.product.api.dto.AdminProductSummaryResponse;
import uy.washop.product.api.dto.ProductAdminResponse;
import uy.washop.product.api.dto.ProductFeatureResponse;
import uy.washop.product.api.dto.ProductImageResponse;
import uy.washop.product.api.dto.ProductPublicImageResponse;
import uy.washop.product.api.dto.ProductPublicResponse;
import uy.washop.product.api.dto.ProductPublicSummaryResponse;
import uy.washop.product.api.dto.ProductVariantAdminResponse;
import uy.washop.product.api.dto.ProductVariantPublicResponse;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductFeature;
import uy.washop.product.domain.ProductImage;
import uy.washop.product.domain.ProductVariant;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static AdminProductSummaryResponse toAdminSummary(
            Product product,
            String primaryImageUrl,
            int variantCount
    ) {
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
                product.getPromoBuyQuantity(),
                product.getPromoPayQuantity(),
                product.getCurrency(),
                product.getStock(),
                variantCount,
                product.isPublished(),
                product.isFeatured(),
                primaryImageUrl,
                product.getUpdatedAt()
        );
    }

    public static ProductPublicSummaryResponse toPublicSummary(
            Product product,
            String primaryImageUrl,
            List<String> availableColors
    ) {
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
                product.getPromoBuyQuantity(),
                product.getPromoPayQuantity(),
                product.getCurrency(),
                product.getStock(),
                product.getWarranty(),
                product.isFeatured(),
                primaryImageUrl,
                product.getCreatedAt(),
                product.getCategoryId(),
                availableColors == null ? List.of() : availableColors
        );
    }

    public static ProductPublicResponse toPublicResponse(
            Product product,
            List<ProductVariant> variants,
            Map<java.util.UUID, List<ProductImage>> imagesByVariant,
            List<ProductFeature> features,
            List<String> compatibleModels
    ) {
        List<ProductVariantPublicResponse> variantResponses = variants.stream()
                .map(variant -> toPublicVariant(
                        variant,
                        imagesByVariant.getOrDefault(variant.getId(), List.of())
                ))
                .toList();

        String primaryImageUrl = variantResponses.stream()
                .filter(ProductVariantPublicResponse::published)
                .map(ProductVariantPublicResponse::primaryImageUrl)
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .orElse(variantResponses.isEmpty() ? null : variantResponses.getFirst().primaryImageUrl());

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
                product.getPromoBuyQuantity(),
                product.getPromoPayQuantity(),
                product.getCurrency(),
                product.getStock(),
                product.getWarranty(),
                product.isPublished(),
                product.isFeatured(),
                product.getCategoryId(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                primaryImageUrl,
                variantResponses,
                features.stream().map(ProductMapper::toFeatureResponse).toList(),
                compatibleModels,
                product.getSeoTitle(),
                product.getMetaDescription(),
                product.isIndexable(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public static ProductAdminResponse toAdminResponse(
            Product product,
            List<ProductVariant> variants,
            Map<java.util.UUID, List<ProductImage>> imagesByVariant,
            List<ProductFeature> features,
            List<String> compatibleModels
    ) {
        List<ProductVariantAdminResponse> variantResponses = variants.stream()
                .map(variant -> toAdminVariant(
                        variant,
                        imagesByVariant.getOrDefault(variant.getId(), List.of())
                ))
                .toList();

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
                product.getPromoBuyQuantity(),
                product.getPromoPayQuantity(),
                product.getCurrency(),
                product.getStock(),
                product.getWarranty(),
                product.isPublished(),
                product.isFeatured(),
                product.getCategoryId(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                variantResponses.size(),
                variantResponses,
                features.stream().map(ProductMapper::toFeatureResponse).toList(),
                compatibleModels,
                product.getSeoTitle(),
                product.getMetaDescription(),
                product.isIndexable(),
                product.getPublishedAt(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public static ProductVariantAdminResponse toAdminVariant(ProductVariant variant, List<ProductImage> images) {
        return new ProductVariantAdminResponse(
                variant.getId(),
                variant.getCondition(),
                variant.getStorageCapacity(),
                variant.getColor(),
                variant.getBatteryHealth(),
                variant.getPrice(),
                variant.getPreviousPrice(),
                variant.getCurrency(),
                variant.getStock(),
                variant.getWarranty(),
                variant.getImei(),
                variant.isPublished(),
                images.stream().map(ProductMapper::toImageResponse).toList(),
                variant.getCreatedAt(),
                variant.getUpdatedAt()
        );
    }

    public static ProductVariantPublicResponse toPublicVariant(ProductVariant variant, List<ProductImage> images) {
        List<ProductPublicImageResponse> imageResponses =
                images.stream().map(ProductMapper::toPublicImageResponse).toList();
        String primaryImageUrl = imageResponses.stream()
                .filter(ProductPublicImageResponse::mainImage)
                .map(ProductPublicImageResponse::url)
                .findFirst()
                .orElse(imageResponses.isEmpty() ? null : imageResponses.getFirst().url());
        return new ProductVariantPublicResponse(
                variant.getId(),
                variant.getCondition(),
                variant.getStorageCapacity(),
                variant.getColor(),
                variant.getBatteryHealth(),
                variant.getPrice(),
                variant.getPreviousPrice(),
                variant.getCurrency(),
                variant.getStock(),
                variant.getWarranty(),
                variant.isPublished(),
                primaryImageUrl,
                imageResponses
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
