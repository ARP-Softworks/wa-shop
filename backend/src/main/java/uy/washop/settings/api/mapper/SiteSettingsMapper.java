package uy.washop.settings.api.mapper;

import uy.washop.settings.api.dto.PublicSiteSettingsResponse;
import uy.washop.settings.api.dto.SiteSettingsResponse;
import uy.washop.settings.domain.SiteSettings;

public final class SiteSettingsMapper {

    private SiteSettingsMapper() {
    }

    public static SiteSettingsResponse toResponse(SiteSettings settings) {
        return new SiteSettingsResponse(
                settings.getId(),
                settings.getBusinessName(),
                settings.getWhatsappNumber(),
                settings.getInstagramUrl(),
                settings.getAddress(),
                settings.getOpeningHours(),
                settings.getContactEmail(),
                settings.getLogoUrl(),
                settings.getLogoPublicId(),
                settings.getPublicSiteUrl(),
                settings.getDefaultSeoTitle(),
                settings.getDefaultMetaDescription(),
                settings.getDefaultSocialImageUrl(),
                settings.getGoogleSiteVerification(),
                settings.getBingSiteVerification(),
                settings.getCountry(),
                settings.getRegion(),
                settings.getCity(),
                settings.getPostalCode(),
                settings.getCreatedAt(),
                settings.getUpdatedAt()
        );
    }

    public static PublicSiteSettingsResponse toPublicResponse(SiteSettings settings, String mercadoPagoPublicKey) {
        return new PublicSiteSettingsResponse(
                settings.getBusinessName(),
                settings.getWhatsappNumber(),
                settings.getInstagramUrl(),
                settings.getAddress(),
                settings.getOpeningHours(),
                settings.getContactEmail(),
                settings.getLogoUrl(),
                settings.getPublicSiteUrl(),
                settings.getDefaultSeoTitle(),
                settings.getDefaultMetaDescription(),
                settings.getDefaultSocialImageUrl(),
                settings.getGoogleSiteVerification(),
                settings.getBingSiteVerification(),
                settings.getCountry(),
                settings.getRegion(),
                settings.getCity(),
                settings.getPostalCode(),
                mercadoPagoPublicKey
        );
    }
}
