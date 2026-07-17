package uy.washop.product.api;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.product.api.dto.AdminProductSummaryResponse;
import uy.washop.product.api.dto.ProductAdminResponse;
import uy.washop.product.api.dto.ProductSearchCriteria;
import uy.washop.product.api.dto.ProductWriteRequest;
import uy.washop.product.api.dto.PublishRequest;
import uy.washop.product.application.AdminProductService;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.shared.api.PageResponse;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping
    public PageResponse<AdminProductSummaryResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) ProductCondition condition,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sort
    ) {
        return adminProductService.search(
                new ProductSearchCriteria(
                        q, productType, condition, model, null, null, null, minPrice, maxPrice, featured, null
                ),
                published,
                page,
                size,
                sort
        );
    }

    @GetMapping("/{id}")
    public ProductAdminResponse get(@PathVariable UUID id) {
        return adminProductService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductAdminResponse create(@Valid @RequestBody ProductWriteRequest request) {
        return adminProductService.create(request);
    }

    @PutMapping("/{id}")
    public ProductAdminResponse update(@PathVariable UUID id, @Valid @RequestBody ProductWriteRequest request) {
        return adminProductService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        adminProductService.delete(id);
    }

    @PatchMapping("/{id}/publish")
    public ProductAdminResponse publish(@PathVariable UUID id, @Valid @RequestBody PublishRequest request) {
        return adminProductService.setPublished(id, request.published());
    }
}
