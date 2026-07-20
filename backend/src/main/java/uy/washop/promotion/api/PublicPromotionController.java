package uy.washop.promotion.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.promotion.api.dto.PublicPromotionResponse;
import uy.washop.promotion.api.mapper.PromotionMapper;
import uy.washop.promotion.infrastructure.PromotionRepository;

@RestController
@RequestMapping("/api/public/promotions")
public class PublicPromotionController {

    private final PromotionRepository promotionRepository;

    public PublicPromotionController(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @GetMapping
    public List<PublicPromotionResponse> listActive() {
        return promotionRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(PromotionMapper::toPublicResponse)
                .toList();
    }
}
