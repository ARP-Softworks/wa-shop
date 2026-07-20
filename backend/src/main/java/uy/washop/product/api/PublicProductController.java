package uy.washop.product.api;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.product.api.dto.ProductPublicResponse;
import uy.washop.product.api.dto.ProductPublicSummaryResponse;
import uy.washop.product.api.dto.ProductSearchCriteria;
import uy.washop.product.application.PublicCatalogService;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.shared.api.PageResponse;

@RestController
@RequestMapping("/api/public/products")
public class PublicProductController {

    private final PublicCatalogService publicCatalogService;

    public PublicProductController(PublicCatalogService publicCatalogService) {
        this.publicCatalogService = publicCatalogService;
    }

    @GetMapping
    public PageResponse<ProductPublicSummaryResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) ProductCondition condition,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String storageCapacity,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Integer minBatteryHealth,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sort
    ) {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                q,
                productType,
                condition,
                model,
                storageCapacity,
                color,
                minBatteryHealth,
                minPrice,
                maxPrice,
                featured,
                inStock
        );
        return publicCatalogService.search(criteria, page, size, sort);
    }

    @GetMapping("/{slugOrId}")
    public ProductPublicResponse getBySlugOrId(@PathVariable String slugOrId) {
        return publicCatalogService.getPublishedBySlugOrId(slugOrId);
    }

    @GetMapping("/{slugOrId}/related")
    public List<ProductPublicSummaryResponse> related(@PathVariable String slugOrId) {
        return publicCatalogService.related(slugOrId);
    }

    @GetMapping("/{slugOrId}/variants")
    public List<ProductPublicSummaryResponse> variants(@PathVariable String slugOrId) {
        return publicCatalogService.variants(slugOrId);
    }
}
