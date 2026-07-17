package uy.washop.product.application;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uy.washop.product.api.dto.ProductPublicResponse;
import uy.washop.product.api.dto.ProductPublicSummaryResponse;
import uy.washop.product.api.dto.ProductSearchCriteria;
import uy.washop.product.api.mapper.ProductMapper;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductFeature;
import uy.washop.product.domain.ProductImage;
import uy.washop.product.infrastructure.ProductFeatureRepository;
import uy.washop.product.infrastructure.ProductImageRepository;
import uy.washop.product.infrastructure.ProductRepository;
import uy.washop.product.infrastructure.ProductSpecifications;
import uy.washop.shared.api.PageResponse;
import uy.washop.shared.exception.ResourceNotFoundException;

@Service
public class PublicCatalogService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductFeatureRepository productFeatureRepository;
    private final PrimaryImageUrlLoader primaryImageUrlLoader;

    public PublicCatalogService(
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            ProductFeatureRepository productFeatureRepository,
            PrimaryImageUrlLoader primaryImageUrlLoader
    ) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productFeatureRepository = productFeatureRepository;
        this.primaryImageUrlLoader = primaryImageUrlLoader;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductPublicSummaryResponse> search(
            ProductSearchCriteria criteria,
            int page,
            int size,
            String sort
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), resolveSort(sort));
        Page<Product> result = productRepository.findAll(ProductSpecifications.fromPublicCriteria(criteria), pageable);

        Map<UUID, String> primaryImages = primaryImageUrlLoader.load(
                result.getContent().stream().map(Product::getId).toList()
        );

        List<ProductPublicSummaryResponse> content = result.getContent().stream()
                .map(product -> ProductMapper.toPublicSummary(product, primaryImages.get(product.getId())))
                .toList();

        return new PageResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public ProductPublicResponse getPublishedBySlugOrId(String slugOrId) {
        Product product = resolvePublished(slugOrId);
        return toDetail(product);
    }

    @Transactional(readOnly = true)
    public List<ProductPublicSummaryResponse> related(String slugOrId) {
        Product product = resolvePublished(slugOrId);
        List<Product> related = productRepository
                .findTop4ByPublishedTrueAndProductTypeAndIdNotOrderByFeaturedDescCreatedAtDesc(
                        product.getProductType(),
                        product.getId()
                );
        Map<UUID, String> primaryImages = primaryImageUrlLoader.load(
                related.stream().map(Product::getId).toList()
        );
        return related.stream()
                .map(item -> ProductMapper.toPublicSummary(item, primaryImages.get(item.getId())))
                .toList();
    }

    private Product resolvePublished(String slugOrId) {
        try {
            UUID id = UUID.fromString(slugOrId);
            return productRepository.findById(id)
                    .filter(Product::isPublished)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        } catch (IllegalArgumentException ignored) {
            return productRepository.findBySlugAndPublishedTrue(slugOrId)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        }
    }

    private ProductPublicResponse toDetail(Product product) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByPositionAsc(product.getId());
        List<ProductFeature> features = productFeatureRepository.findByProductIdOrderByNameAsc(product.getId());
        return ProductMapper.toPublicResponse(product, images, features);
    }

    private static int clampSize(int size) {
        if (size < 1) {
            return 12;
        }
        return Math.min(size, 48);
    }

    private static Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Order.desc("featured"), Sort.Order.desc("createdAt"));
        }
        String normalized = sort.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "price,asc", "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price,desc", "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "createdat,asc", "date_asc" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "createdat,desc", "date_desc" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Order.desc("featured"), Sort.Order.desc("createdAt"));
        };
    }
}
