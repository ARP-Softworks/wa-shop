package uy.washop.seo.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.seo.api.dto.SeoPageMetaResponse;
import uy.washop.seo.application.SeoPageService;

@RestController
@RequestMapping("/api/public/seo")
public class PublicSeoPageController {

    private final SeoPageService seoPageService;

    public PublicSeoPageController(SeoPageService seoPageService) {
        this.seoPageService = seoPageService;
    }

    @GetMapping("/page")
    public SeoPageMetaResponse page(@RequestParam("path") String path) {
        return seoPageService.resolvePage(path);
    }
}
