package uy.washop.product.api.dto;

import java.util.UUID;

/** Public product image projection — excludes storage {@code publicId} and binary metadata. */
public record ProductPublicImageResponse(
        UUID id,
        String url,
        String altText,
        int position,
        boolean mainImage,
        Integer width,
        Integer height
) {
}
