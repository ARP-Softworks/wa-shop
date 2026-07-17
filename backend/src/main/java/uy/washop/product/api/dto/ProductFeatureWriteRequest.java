package uy.washop.product.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductFeatureWriteRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 500) String value
) {
}
