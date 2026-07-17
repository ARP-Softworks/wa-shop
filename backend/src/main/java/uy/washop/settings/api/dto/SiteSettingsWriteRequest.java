package uy.washop.settings.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SiteSettingsWriteRequest(
        @NotBlank @Size(max = 200) String businessName,
        @Size(max = 40) String whatsappNumber,
        @Size(max = 500) String instagramUrl,
        @Size(max = 500) String address,
        @Size(max = 500) String openingHours,
        @Email @Size(max = 320) String contactEmail,
        String logoUrl,
        @Size(max = 255) String logoPublicId,
        @Size(max = 500) String publicSiteUrl,
        @Size(max = 70) String defaultSeoTitle,
        @Size(max = 320) String defaultMetaDescription,
        String defaultSocialImageUrl,
        @Size(max = 120) String googleSiteVerification,
        @Size(max = 120) String bingSiteVerification,
        @Size(max = 80) String country,
        @Size(max = 120) String region,
        @Size(max = 120) String city,
        @Size(max = 40) String postalCode
) {
}
