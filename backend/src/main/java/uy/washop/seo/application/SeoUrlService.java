package uy.washop.seo.application;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uy.washop.config.AppProperties;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.settings.domain.SiteSettings;
import uy.washop.settings.infrastructure.SiteSettingsRepository;

@Service
public class SeoUrlService {

    private final AppProperties appProperties;
    private final SiteSettingsRepository siteSettingsRepository;

    public SeoUrlService(AppProperties appProperties, SiteSettingsRepository siteSettingsRepository) {
        this.appProperties = appProperties;
        this.siteSettingsRepository = siteSettingsRepository;
    }

    public String resolvePublicSiteUrl() {
        SiteSettings settings = siteSettingsRepository.findById(SiteSettings.DEFAULT_ID).orElse(null);
        if (settings != null && StringUtils.hasText(settings.getPublicSiteUrl())) {
            return trimTrailingSlash(settings.getPublicSiteUrl().trim());
        }
        if (StringUtils.hasText(appProperties.getPublicSiteUrl())) {
            return trimTrailingSlash(appProperties.getPublicSiteUrl().trim());
        }
        return trimTrailingSlash(appProperties.getPublicBaseUrl().trim());
    }

    public String absoluteUrl(String path) {
        String base = resolvePublicSiteUrl();
        if (!StringUtils.hasText(path) || "/".equals(path)) {
            return base + "/";
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        return base + normalized;
    }

    public String productPath(Product product) {
        if (product.getProductType() == ProductType.ACCESSORY) {
            return "/accesorios/" + product.getSlug();
        }
        return "/iphone/" + product.getSlug();
    }

    public String productPath(ProductType productType, String slug) {
        if (productType == ProductType.ACCESSORY) {
            return "/accesorios/" + slug;
        }
        return "/iphone/" + slug;
    }

    public String categoryPath(String slug) {
        return "/accesorios/categoria/" + slug;
    }

    public String technicalServicePath(String slug) {
        return "/servicio-tecnico/" + slug;
    }

    public String iphoneConditionPath(ProductCondition condition) {
        if (condition == ProductCondition.USED) {
            return "/iphone/usados";
        }
        return "/iphone/nuevos";
    }

    public String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public String escapeHtmlAttribute(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public static String trimTrailingSlash(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        String trimmed = url.trim();
        while (trimmed.endsWith("/") && trimmed.length() > 1 && !trimmed.endsWith("://")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    public static String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "/";
        }
        String trimmed = path.trim();
        int query = trimmed.indexOf('?');
        if (query >= 0) {
            trimmed = trimmed.substring(0, query);
        }
        int hash = trimmed.indexOf('#');
        if (hash >= 0) {
            trimmed = trimmed.substring(0, hash);
        }
        if (!trimmed.startsWith("/")) {
            trimmed = "/" + trimmed;
        }
        if (trimmed.length() > 1 && trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
