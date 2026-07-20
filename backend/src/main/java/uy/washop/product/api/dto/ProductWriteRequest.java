package uy.washop.product.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import uy.washop.product.domain.ProductType;

public record ProductWriteRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 220) String slug,
        @Size(max = 120) String model,
        String description,
        @NotNull ProductType productType,
        @Min(1) Integer promoBuyQuantity,
        @Min(1) Integer promoPayQuantity,
        boolean published,
        boolean featured,
        UUID categoryId,
        @Size(max = 70) String seoTitle,
        @Size(max = 320) String metaDescription,
        Boolean indexable,
        @Valid List<ProductFeatureWriteRequest> features,
        List<String> compatibleModels,
        @NotEmpty @Valid List<ProductVariantWriteRequest> variants
) {
}
