package uy.washop.product.api.dto;

import java.util.UUID;

public record ProductImageResponse(
        UUID id,
        String url,
        String publicId,
        String altText,
        int position,
        boolean mainImage,
        String format,
        Long sizeBytes,
        Integer width,
        Integer height
) {
}
