package uy.washop.settings.api.dto;

public record PublicSiteSettingsResponse(
        String businessName,
        String whatsappNumber,
        String instagramUrl,
        String address,
        String openingHours,
        String contactEmail,
        String logoUrl,
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
        String mercadoPagoPublicKey
) {
}
