package uy.washop.banner.api.dto;

import java.util.UUID;

public record PublicHeroBannerResponse(
        UUID id,
        String imageUrl,
        String altText,
        String linkUrl
) {
}
