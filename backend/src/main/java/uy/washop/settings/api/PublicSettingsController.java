package uy.washop.settings.api;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.config.AppProperties;
import uy.washop.settings.api.dto.PublicSiteSettingsResponse;
import uy.washop.settings.api.mapper.SiteSettingsMapper;
import uy.washop.settings.domain.SiteSettings;
import uy.washop.settings.infrastructure.SiteSettingsRepository;
import uy.washop.shared.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/public/settings")
public class PublicSettingsController {

    private final SiteSettingsRepository siteSettingsRepository;
    private final AppProperties appProperties;

    public PublicSettingsController(SiteSettingsRepository siteSettingsRepository, AppProperties appProperties) {
        this.siteSettingsRepository = siteSettingsRepository;
        this.appProperties = appProperties;
    }

    @GetMapping
    public PublicSiteSettingsResponse getPublicSettings() {
        SiteSettings settings = siteSettingsRepository.findById(SiteSettings.DEFAULT_ID)
                .or(() -> siteSettingsRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new ResourceNotFoundException("Configuración no disponible"));

        String mercadoPagoPublicKey = appProperties.getPayment().getMercadoPago().getPublicKey();
        return SiteSettingsMapper.toPublicResponse(
                settings,
                StringUtils.hasText(mercadoPagoPublicKey) ? mercadoPagoPublicKey : null
        );
    }
}
