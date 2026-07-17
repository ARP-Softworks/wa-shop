package uy.washop.seo.api.dto;

public record PublicSeoConfigResponse(
        String publicSiteUrl,
        String defaultSeoTitle,
        String defaultMetaDescription,
        String defaultSocialImageUrl,
        String businessName,
        String logoUrl,
        String whatsapp,
        String address,
        String hours,
        String email,
        String instagram,
        String country,
        String region,
        String city,
        String postalCode,
        String googleSiteVerification,
        String bingSiteVerification
) {
}
