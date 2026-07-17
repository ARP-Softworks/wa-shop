package uy.washop.seo.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import uy.washop.seo.api.dto.PublicSeoConfigResponse;
import uy.washop.seo.api.dto.SeoPageMetaResponse;
import uy.washop.seo.application.SeoPageService;
import uy.washop.seo.application.SeoUrlService;
import uy.washop.seo.application.UrlRedirectService;
import uy.washop.seo.domain.UrlRedirect;

@Component
public class SeoHtmlDocumentFilter extends OncePerRequestFilter implements Ordered {

    private static final Set<String> ASSET_EXTENSIONS = Set.of(
            ".js", ".css", ".ico", ".map", ".woff", ".woff2", ".ttf", ".eot",
            ".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg", ".json", ".txt", ".xml"
    );

    private final UrlRedirectService urlRedirectService;
    private final SeoPageService seoPageService;
    private final SeoUrlService seoUrlService;

    public SeoHtmlDocumentFilter(
            UrlRedirectService urlRedirectService,
            SeoPageService seoPageService,
            SeoUrlService seoUrlService
    ) {
        this.urlRedirectService = urlRedirectService;
        this.seoPageService = seoPageService;
        this.seoUrlService = seoUrlService;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!shouldProcess(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = SeoUrlService.normalizePath(request.getRequestURI());

        UrlRedirect redirect = urlRedirectService.findActiveBySourcePath(path);
        if (redirect == null && request.getRequestURI().endsWith("/") && path.length() > 1) {
            redirect = urlRedirectService.findActiveBySourcePath(path + "/");
        }
        String legacyDestination = legacyCatalogRedirect(path);
        if (redirect != null || legacyDestination != null) {
            String destination = redirect != null ? redirect.getDestinationPath() : legacyDestination;
            int statusCode = redirect != null ? redirect.getStatusCode() : HttpStatus.MOVED_PERMANENTLY.value();
            String location = destination.startsWith("http://") || destination.startsWith("https://")
                    ? destination
                    : seoUrlService.absoluteUrl(destination);
            response.setStatus(statusCode);
            response.setHeader(HttpHeaders.LOCATION, location);
            return;
        }

        ClassPathResource index = new ClassPathResource("/static/index.html");
        if (!index.exists() || !index.isReadable()) {
            filterChain.doFilter(request, response);
            return;
        }

        SeoPageMetaResponse meta = seoPageService.resolvePage(path);
        PublicSeoConfigResponse config = seoPageService.publicConfig();
        String html;
        try (InputStream in = index.getInputStream()) {
            html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        String injected = injectHead(html, meta, config);
        if (meta.notFound()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        } else {
            response.setStatus(HttpStatus.OK.value());
        }
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        byte[] body = injected.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(body.length);
        response.getOutputStream().write(body);
    }

    /**
     * Built-in permanent redirects from legacy {@code /catalogo} URLs to {@code /iphone}.
     * Table-driven redirects still take precedence when present.
     */
    private static String legacyCatalogRedirect(String path) {
        if ("/catalogo".equals(path)) {
            return "/iphone";
        }
        if (path.startsWith("/catalogo/")) {
            String rest = path.substring("/catalogo/".length());
            if (StringUtils.hasText(rest) && !rest.contains("/")) {
                return "/iphone/" + rest;
            }
        }
        return null;
    }

    private boolean shouldProcess(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        if (!StringUtils.hasText(path)) {
            return false;
        }
        String normalized = path.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("/api")
                || normalized.startsWith("/media")
                || normalized.startsWith("/actuator")
                || normalized.equals("/robots.txt")
                || normalized.equals("/sitemap.xml")) {
            return false;
        }
        for (String extension : ASSET_EXTENSIONS) {
            if (normalized.endsWith(extension)) {
                return false;
            }
        }
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (!StringUtils.hasText(accept)) {
            return true;
        }
        String lower = accept.toLowerCase(Locale.ROOT);
        return lower.contains(MediaType.TEXT_HTML_VALUE) || lower.contains("*/*");
    }

    private String injectHead(String html, SeoPageMetaResponse meta, PublicSeoConfigResponse config) {
        StringBuilder tags = new StringBuilder();
        if (StringUtils.hasText(meta.title())) {
            tags.append("<title>").append(seoUrlService.escapeXml(meta.title())).append("</title>\n");
        }
        if (StringUtils.hasText(meta.description())) {
            tags.append("<meta name=\"description\" content=\"")
                    .append(seoUrlService.escapeHtmlAttribute(meta.description()))
                    .append("\"/>\n");
        }
        if (StringUtils.hasText(meta.robots())) {
            tags.append("<meta name=\"robots\" content=\"")
                    .append(seoUrlService.escapeHtmlAttribute(meta.robots()))
                    .append("\"/>\n");
        }
        if (StringUtils.hasText(meta.canonical())) {
            tags.append("<link rel=\"canonical\" href=\"")
                    .append(seoUrlService.escapeHtmlAttribute(meta.canonical()))
                    .append("\"/>\n");
            tags.append("<meta property=\"og:url\" content=\"")
                    .append(seoUrlService.escapeHtmlAttribute(meta.canonical()))
                    .append("\"/>\n");
        }
        if (StringUtils.hasText(meta.title())) {
            tags.append("<meta property=\"og:title\" content=\"")
                    .append(seoUrlService.escapeHtmlAttribute(meta.title()))
                    .append("\"/>\n");
            tags.append("<meta name=\"twitter:title\" content=\"")
                    .append(seoUrlService.escapeHtmlAttribute(meta.title()))
                    .append("\"/>\n");
        }
        if (StringUtils.hasText(meta.description())) {
            tags.append("<meta property=\"og:description\" content=\"")
                    .append(seoUrlService.escapeHtmlAttribute(meta.description()))
                    .append("\"/>\n");
            tags.append("<meta name=\"twitter:description\" content=\"")
                    .append(seoUrlService.escapeHtmlAttribute(meta.description()))
                    .append("\"/>\n");
        }
        tags.append("<meta property=\"og:type\" content=\"website\"/>\n");
        tags.append("<meta property=\"og:locale\" content=\"es_UY\"/>\n");
        if (StringUtils.hasText(config.businessName())) {
            tags.append("<meta property=\"og:site_name\" content=\"")
                    .append(seoUrlService.escapeHtmlAttribute(config.businessName()))
                    .append("\"/>\n");
        }
        tags.append("<meta name=\"twitter:card\" content=\"summary_large_image\"/>\n");
        if (StringUtils.hasText(meta.ogImage())) {
            tags.append("<meta property=\"og:image\" content=\"")
                    .append(seoUrlService.escapeHtmlAttribute(meta.ogImage()))
                    .append("\"/>\n");
            tags.append("<meta name=\"twitter:image\" content=\"")
                    .append(seoUrlService.escapeHtmlAttribute(meta.ogImage()))
                    .append("\"/>\n");
        }
        if (StringUtils.hasText(meta.ogImageAlt())) {
            tags.append("<meta property=\"og:image:alt\" content=\"")
                    .append(seoUrlService.escapeHtmlAttribute(meta.ogImageAlt()))
                    .append("\"/>\n");
        }
        if (StringUtils.hasText(config.googleSiteVerification())) {
            tags.append("<meta name=\"google-site-verification\" content=\"")
                    .append(seoUrlService.escapeHtmlAttribute(config.googleSiteVerification()))
                    .append("\"/>\n");
        }
        if (StringUtils.hasText(config.bingSiteVerification())) {
            tags.append("<meta name=\"msvalidate.01\" content=\"")
                    .append(seoUrlService.escapeHtmlAttribute(config.bingSiteVerification()))
                    .append("\"/>\n");
        }
        if (meta.jsonLd() != null) {
            for (String script : meta.jsonLd()) {
                if (StringUtils.hasText(script)) {
                    tags.append("<script type=\"application/ld+json\">")
                            .append(script)
                            .append("</script>\n");
                }
            }
        }

        String injection = tags.toString();
        int headClose = html.toLowerCase(Locale.ROOT).indexOf("</head>");
        if (headClose < 0) {
            return html;
        }
        String withoutTitle = html.replaceAll("(?i)<title>[\\s\\S]*?</title>", "");
        headClose = withoutTitle.toLowerCase(Locale.ROOT).indexOf("</head>");
        if (headClose < 0) {
            return html;
        }
        return withoutTitle.substring(0, headClose) + injection + withoutTitle.substring(headClose);
    }
}
