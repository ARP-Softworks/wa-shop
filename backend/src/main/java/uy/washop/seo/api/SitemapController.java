package uy.washop.seo.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.seo.application.SitemapService;

@RestController
public class SitemapController {

    private final SitemapService sitemapService;

    public SitemapController(SitemapService sitemapService) {
        this.sitemapService = sitemapService;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        return sitemapService.buildSitemapXml();
    }
}
