package uy.washop.technicalservice.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import uy.washop.shared.domain.CurrencyCode;

public record TechnicalServiceWriteRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 220) String slug,
        String description,
        @DecimalMin("0.00") BigDecimal price,
        CurrencyCode currency,
        @Size(max = 120) String estimatedTime,
        boolean active,
        @Size(max = 70) String seoTitle,
        @Size(max = 320) String metaDescription,
        Boolean indexable
) {
}
