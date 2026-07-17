package uy.washop.product.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductImageWriteRequest(
        @NotBlank String url,
        @Size(max = 255) String publicId,
        @Size(max = 255) String altText,
        @Min(0) int position,
        boolean mainImage,
        @Size(max = 20) String format,
        Long sizeBytes,
        Integer width,
        Integer height
) {
}
