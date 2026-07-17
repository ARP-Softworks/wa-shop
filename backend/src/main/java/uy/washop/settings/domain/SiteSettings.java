package uy.washop.settings.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import uy.washop.shared.domain.BaseEntity;

@Entity
@Table(name = "site_settings")
public class SiteSettings extends BaseEntity {

    public static final java.util.UUID DEFAULT_ID =
            java.util.UUID.fromString("55555555-5555-5555-5555-555555555555");

    @NotBlank
    @Size(max = 200)
    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Size(max = 40)
    @Column(name = "whatsapp_number", length = 40)
    private String whatsappNumber;

    @Size(max = 500)
    @Column(name = "instagram_url", length = 500)
    private String instagramUrl;

    @Size(max = 500)
    @Column(length = 500)
    private String address;

    @Size(max = 500)
    @Column(name = "opening_hours", length = 500)
    private String openingHours;

    @Email
    @Size(max = 320)
    @Column(name = "contact_email", length = 320)
    private String contactEmail;

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    @Size(max = 255)
    @Column(name = "logo_public_id", length = 255)
    private String logoPublicId;

    @Size(max = 500)
    @Column(name = "public_site_url", length = 500)
    private String publicSiteUrl;

    @Size(max = 70)
    @Column(name = "default_seo_title", length = 70)
    private String defaultSeoTitle;

    @Size(max = 320)
    @Column(name = "default_meta_description", length = 320)
    private String defaultMetaDescription;

    @Column(name = "default_social_image_url", columnDefinition = "TEXT")
    private String defaultSocialImageUrl;

    @Size(max = 120)
    @Column(name = "google_site_verification", length = 120)
    private String googleSiteVerification;

    @Size(max = 120)
    @Column(name = "bing_site_verification", length = 120)
    private String bingSiteVerification;

    @Size(max = 80)
    @Column(length = 80)
    private String country;

    @Size(max = 120)
    @Column(length = 120)
    private String region;

    @Size(max = 120)
    @Column(length = 120)
    private String city;

    @Size(max = 40)
    @Column(name = "postal_code", length = 40)
    private String postalCode;

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getWhatsappNumber() {
        return whatsappNumber;
    }

    public void setWhatsappNumber(String whatsappNumber) {
        this.whatsappNumber = whatsappNumber;
    }

    public String getInstagramUrl() {
        return instagramUrl;
    }

    public void setInstagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getLogoPublicId() {
        return logoPublicId;
    }

    public void setLogoPublicId(String logoPublicId) {
        this.logoPublicId = logoPublicId;
    }

    public String getPublicSiteUrl() {
        return publicSiteUrl;
    }

    public void setPublicSiteUrl(String publicSiteUrl) {
        this.publicSiteUrl = publicSiteUrl;
    }

    public String getDefaultSeoTitle() {
        return defaultSeoTitle;
    }

    public void setDefaultSeoTitle(String defaultSeoTitle) {
        this.defaultSeoTitle = defaultSeoTitle;
    }

    public String getDefaultMetaDescription() {
        return defaultMetaDescription;
    }

    public void setDefaultMetaDescription(String defaultMetaDescription) {
        this.defaultMetaDescription = defaultMetaDescription;
    }

    public String getDefaultSocialImageUrl() {
        return defaultSocialImageUrl;
    }

    public void setDefaultSocialImageUrl(String defaultSocialImageUrl) {
        this.defaultSocialImageUrl = defaultSocialImageUrl;
    }

    public String getGoogleSiteVerification() {
        return googleSiteVerification;
    }

    public void setGoogleSiteVerification(String googleSiteVerification) {
        this.googleSiteVerification = googleSiteVerification;
    }

    public String getBingSiteVerification() {
        return bingSiteVerification;
    }

    public void setBingSiteVerification(String bingSiteVerification) {
        this.bingSiteVerification = bingSiteVerification;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
