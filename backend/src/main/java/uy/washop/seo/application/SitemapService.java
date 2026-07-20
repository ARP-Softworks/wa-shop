package uy.washop.seo.application;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uy.washop.category.domain.Category;
import uy.washop.category.infrastructure.CategoryRepository;
import uy.washop.product.domain.Product;
import uy.washop.product.infrastructure.ProductRepository;
import uy.washop.technicalservice.domain.TechnicalService;
import uy.washop.technicalservice.infrastructure.TechnicalServiceRepository;

/**
 * Builds the public sitemap. When the catalog grows large, split into a sitemap index
 * ({@code sitemap-index.xml} + partitioned sitemaps) instead of a single urlset.
 */
@Service
public class SitemapService {

    private static final long CACHE_TTL_MS = 60_000L;
    private static final DateTimeFormatter LASTMOD =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX").withZone(ZoneOffset.UTC);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TechnicalServiceRepository technicalServiceRepository;
    private final SeoUrlService seoUrlService;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public SitemapService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            TechnicalServiceRepository technicalServiceRepository,
            SeoUrlService seoUrlService
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.technicalServiceRepository = technicalServiceRepository;
        this.seoUrlService = seoUrlService;
    }

    @Transactional(readOnly = true)
    public String buildSitemapXml() {
        CacheEntry cached = cache.get("sitemap");
        long now = System.currentTimeMillis();
        if (cached != null && now - cached.createdAtMs() < CACHE_TTL_MS) {
            return cached.xml();
        }
        String xml = render();
        cache.put("sitemap", new CacheEntry(xml, now));
        return xml;
    }

    public void invalidateCache() {
        cache.clear();
    }

    private String render() {
        List<SitemapEntry> entries = new ArrayList<>();
        Instant now = Instant.now();
        addStatic(entries, "/", now);
        addStatic(entries, "/iphone", now);
        addStatic(entries, "/iphone/nuevos", now);
        addStatic(entries, "/iphone/usados", now);
        addStatic(entries, "/accesorios", now);
        addStatic(entries, "/servicio-tecnico", now);
        addStatic(entries, "/contacto", now);
        addStatic(entries, "/preguntas-frecuentes", now);

        for (Category category : categoryRepository.findByActiveTrueAndIndexableTrueOrderByNameAsc()) {
            entries.add(new SitemapEntry(
                    seoUrlService.categoryPath(category.getSlug()),
                    category.getUpdatedAt() != null ? category.getUpdatedAt() : now
            ));
        }

        for (Product product : productRepository.findByPublishedTrueAndIndexableTrueAndStockGreaterThanOrderByUpdatedAtDesc(0)) {
            entries.add(new SitemapEntry(
                    seoUrlService.productPath(product),
                    product.getUpdatedAt() != null ? product.getUpdatedAt() : now
            ));
        }

        for (TechnicalService service : technicalServiceRepository.findByActiveTrueAndIndexableTrueOrderByNameAsc()) {
            entries.add(new SitemapEntry(
                    seoUrlService.technicalServicePath(service.getSlug()),
                    service.getUpdatedAt() != null ? service.getUpdatedAt() : now
            ));
        }

        StringBuilder xml = new StringBuilder(2048);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        for (SitemapEntry entry : entries) {
            xml.append("  <url>\n");
            xml.append("    <loc>").append(seoUrlService.escapeXml(seoUrlService.absoluteUrl(entry.path()))).append("</loc>\n");
            xml.append("    <lastmod>").append(LASTMOD.format(entry.lastmod())).append("</lastmod>\n");
            xml.append("  </url>\n");
        }
        xml.append("</urlset>\n");
        return xml.toString();
    }

    private void addStatic(List<SitemapEntry> entries, String path, Instant lastmod) {
        entries.add(new SitemapEntry(path, lastmod));
    }

    private record SitemapEntry(String path, Instant lastmod) {
    }

    private record CacheEntry(String xml, long createdAtMs) {
    }
}
