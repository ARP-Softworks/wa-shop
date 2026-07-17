package uy.washop.settings.api.dto;

import java.time.Instant;
import java.util.UUID;

public record SiteSettingsResponse(
        UUID id,
        String businessName,
        String whatsappNumber,
        String instagramUrl,
        String address,
        String openingHours,
        String contactEmail,
        String logoUrl,
        String logoPublicId,
        String publicSiteUrl,
        String defaultSeoTitle,
        String defaultMetaDescription,
        String defaultSocialImageUrl,
        String googleSiteVerification,
        String bingSiteVerification,
        String country,
        String region,
        String city,
        String postalCode,
        Instant createdAt,
        Instant updatedAt
) {
}
