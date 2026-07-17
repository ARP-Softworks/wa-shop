package uy.washop.seo.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.seo.application.SeoUrlService;

@RestController
public class RobotsController {

    private final SeoUrlService seoUrlService;

    public RobotsController(SeoUrlService seoUrlService) {
        this.seoUrlService = seoUrlService;
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String robots() {
        String sitemap = seoUrlService.absoluteUrl("/sitemap.xml");
        return """
                User-agent: *
                Allow: /
                Disallow: /admin
                Disallow: /admin/
                Disallow: /api/
                Disallow: /login
                Sitemap: %s
                """.formatted(sitemap);
    }
}
