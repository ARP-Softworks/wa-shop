package uy.washop.banner.api.mapper;

import uy.washop.banner.api.dto.HeroBannerResponse;
import uy.washop.banner.api.dto.PublicHeroBannerResponse;
import uy.washop.banner.domain.HeroBanner;

public final class HeroBannerMapper {

    private HeroBannerMapper() {
    }

    public static HeroBannerResponse toResponse(HeroBanner banner) {
        return new HeroBannerResponse(
                banner.getId(),
                banner.getImageUrl(),
                banner.getImagePublicId(),
                banner.getAltText(),
                banner.getLinkUrl(),
                banner.getPosition(),
                banner.isActive(),
                banner.getCreatedAt(),
                banner.getUpdatedAt()
        );
    }

    public static PublicHeroBannerResponse toPublicResponse(HeroBanner banner) {
        return new PublicHeroBannerResponse(
                banner.getId(),
                banner.getImageUrl(),
                banner.getAltText(),
                banner.getLinkUrl()
        );
    }
}
