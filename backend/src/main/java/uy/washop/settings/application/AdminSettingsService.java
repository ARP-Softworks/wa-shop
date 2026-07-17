package uy.washop.settings.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.media.application.MediaApplicationService;
import uy.washop.seo.application.SitemapService;
import uy.washop.settings.api.dto.SiteSettingsResponse;
import uy.washop.settings.api.dto.SiteSettingsWriteRequest;
import uy.washop.settings.api.mapper.SiteSettingsMapper;
import uy.washop.settings.domain.SiteSettings;
import uy.washop.settings.infrastructure.SiteSettingsRepository;
import uy.washop.shared.exception.ResourceNotFoundException;

@Service
public class AdminSettingsService {

    private final SiteSettingsRepository siteSettingsRepository;
    private final AuditService auditService;
    private final MediaApplicationService mediaApplicationService;
    private final SitemapService sitemapService;

    public AdminSettingsService(
            SiteSettingsRepository siteSettingsRepository,
            AuditService auditService,
            MediaApplicationService mediaApplicationService,
            SitemapService sitemapService
    ) {
        this.siteSettingsRepository = siteSettingsRepository;
        this.auditService = auditService;
        this.mediaApplicationService = mediaApplicationService;
        this.sitemapService = sitemapService;
    }

    @Transactional(readOnly = true)
    public SiteSettingsResponse get() {
        return SiteSettingsMapper.toResponse(requireSettings());
    }

    @Transactional
    public SiteSettingsResponse update(SiteSettingsWriteRequest request) {
        SiteSettings settings = requireSettings();
        String previousLogoPublicId = settings.getLogoPublicId();

        settings.setBusinessName(request.businessName().trim());
        settings.setWhatsappNumber(blankToNull(request.whatsappNumber()));
        settings.setInstagramUrl(blankToNull(request.instagramUrl()));
        settings.setAddress(blankToNull(request.address()));
        settings.setOpeningHours(blankToNull(request.openingHours()));
        settings.setContactEmail(blankToNull(request.contactEmail()));
        settings.setLogoUrl(blankToNull(request.logoUrl()));
        settings.setLogoPublicId(blankToNull(request.logoPublicId()));
        settings.setPublicSiteUrl(blankToNull(request.publicSiteUrl()));
        settings.setDefaultSeoTitle(blankToNull(request.defaultSeoTitle()));
        settings.setDefaultMetaDescription(blankToNull(request.defaultMetaDescription()));
        settings.setDefaultSocialImageUrl(blankToNull(request.defaultSocialImageUrl()));
        settings.setGoogleSiteVerification(blankToNull(request.googleSiteVerification()));
        settings.setBingSiteVerification(blankToNull(request.bingSiteVerification()));
        settings.setCountry(blankToNull(request.country()));
        settings.setRegion(blankToNull(request.region()));
        settings.setCity(blankToNull(request.city()));
        settings.setPostalCode(blankToNull(request.postalCode()));
        settings = siteSettingsRepository.save(settings);
        sitemapService.invalidateCache();
        auditService.record(AuditAction.UPDATE, "SiteSettings", settings.getId(), "Configuración del sitio actualizada");

        if (StringUtils.hasText(previousLogoPublicId)
                && !previousLogoPublicId.equals(settings.getLogoPublicId())) {
            mediaApplicationService.tryDeleteIfUnreferenced(previousLogoPublicId);
        }

        return SiteSettingsMapper.toResponse(settings);
    }

    private SiteSettings requireSettings() {
        return siteSettingsRepository.findById(SiteSettings.DEFAULT_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración del sitio no encontrada"));
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
