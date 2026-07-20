package uy.washop.banner.api.dto;

import java.time.Instant;
import java.util.UUID;

public record HeroBannerResponse(
        UUID id,
        String imageUrl,
        String imagePublicId,
        String altText,
        String linkUrl,
        int position,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
