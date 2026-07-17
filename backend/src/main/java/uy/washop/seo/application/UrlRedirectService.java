package uy.washop.seo.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uy.washop.seo.domain.UrlRedirect;
import uy.washop.seo.infrastructure.UrlRedirectRepository;

@Service
public class UrlRedirectService {

    private final UrlRedirectRepository urlRedirectRepository;
    private final SitemapService sitemapService;

    public UrlRedirectService(UrlRedirectRepository urlRedirectRepository, SitemapService sitemapService) {
        this.urlRedirectRepository = urlRedirectRepository;
        this.sitemapService = sitemapService;
    }

    @Transactional(readOnly = true)
    public UrlRedirect findActiveBySourcePath(String sourcePath) {
        String normalized = SeoUrlService.normalizePath(sourcePath);
        return urlRedirectRepository.findBySourcePathAndActiveTrue(normalized)
                .or(() -> {
                    if (!normalized.equals("/") && !sourcePath.endsWith("/")) {
                        return urlRedirectRepository.findBySourcePathAndActiveTrue(normalized + "/");
                    }
                    return java.util.Optional.empty();
                })
                .orElse(null);
    }

    @Transactional
    public void createOrUpdateRedirect(String sourcePath, String destinationPath, int statusCode) {
        String source = SeoUrlService.normalizePath(sourcePath);
        String destination = SeoUrlService.normalizePath(destinationPath);
        if (!StringUtils.hasText(source) || !StringUtils.hasText(destination) || source.equals(destination)) {
            return;
        }
        if (!source.startsWith("/") || !destination.startsWith("/")) {
            return;
        }

        UrlRedirect redirect = urlRedirectRepository.findBySourcePath(source).orElseGet(UrlRedirect::new);
        redirect.setSourcePath(source);
        redirect.setDestinationPath(destination);
        redirect.setStatusCode(statusCode);
        redirect.setActive(true);
        urlRedirectRepository.save(redirect);
        sitemapService.invalidateCache();
    }

    @Transactional
    public void redirectSlugChange(String oldPath, String newPath) {
        createOrUpdateRedirect(oldPath, newPath, 301);
    }
}
