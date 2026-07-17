package uy.washop.seo.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.seo.api.dto.PublicSeoConfigResponse;
import uy.washop.seo.application.SeoPageService;

@RestController
@RequestMapping("/api/public/seo")
public class PublicSeoController {

    private final SeoPageService seoPageService;

    public PublicSeoController(SeoPageService seoPageService) {
        this.seoPageService = seoPageService;
    }

    @GetMapping("/config")
    public PublicSeoConfigResponse config() {
        return seoPageService.publicConfig();
    }
}
