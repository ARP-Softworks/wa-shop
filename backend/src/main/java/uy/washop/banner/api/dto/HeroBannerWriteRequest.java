package uy.washop.banner.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HeroBannerWriteRequest(
        @NotBlank String imageUrl,
        String imagePublicId,
        @Size(max = 255) String altText,
        @Size(max = 500) String linkUrl,
        int position,
        boolean active
) {
}
