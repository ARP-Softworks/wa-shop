package uy.washop.banner.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.banner.api.dto.PublicHeroBannerResponse;
import uy.washop.banner.api.mapper.HeroBannerMapper;
import uy.washop.banner.infrastructure.HeroBannerRepository;

@RestController
@RequestMapping("/api/public/hero-banners")
public class PublicHeroBannerController {

    private final HeroBannerRepository heroBannerRepository;

    public PublicHeroBannerController(HeroBannerRepository heroBannerRepository) {
        this.heroBannerRepository = heroBannerRepository;
    }

    @GetMapping
    public List<PublicHeroBannerResponse> listActive() {
        return heroBannerRepository.findByActiveTrueOrderByPositionAsc().stream()
                .map(HeroBannerMapper::toPublicResponse)
                .toList();
    }
}
