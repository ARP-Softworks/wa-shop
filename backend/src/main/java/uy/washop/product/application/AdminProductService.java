package uy.washop.product.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.category.domain.Category;
import uy.washop.category.infrastructure.CategoryRepository;
import uy.washop.media.application.MediaApplicationService;
import uy.washop.product.api.dto.AdminProductSummaryResponse;
import uy.washop.product.api.dto.ProductAdminResponse;
import uy.washop.product.api.dto.ProductFeatureWriteRequest;
import uy.washop.product.api.dto.ProductImageWriteRequest;
import uy.washop.product.api.dto.ProductSearchCriteria;
import uy.washop.product.api.dto.ProductWriteRequest;
import uy.washop.product.api.mapper.ProductMapper;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductFeature;
import uy.washop.product.domain.ProductImage;
import uy.washop.product.domain.ProductImageRules;
import uy.washop.product.domain.ProductRules;
import uy.washop.product.domain.ProductType;
import uy.washop.product.infrastructure.ProductFeatureRepository;
import uy.washop.product.infrastructure.ProductImageRepository;
import uy.washop.product.infrastructure.ProductRepository;
import uy.washop.seo.application.SitemapService;
import uy.washop.seo.application.SeoUrlService;
import uy.washop.seo.application.UrlRedirectService;
import uy.washop.shared.api.PageResponse;
import uy.washop.shared.exception.BusinessConflictException;
import uy.washop.shared.exception.ResourceNotFoundException;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductFeatureRepository productFeatureRepository;
    private final CategoryRepository categoryRepository;
    private final AuditService auditService;
    private final MediaApplicationService mediaApplicationService;
    private final PrimaryImageUrlLoader primaryImageUrlLoader;
    private final UrlRedirectService urlRedirectService;
    private final SeoUrlService seoUrlService;
    private final SitemapService sitemapService;

    public AdminProductService(
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            ProductFeatureRepository productFeatureRepository,
            CategoryRepository categoryRepository,
            AuditService auditService,
            MediaApplicationService mediaApplicationService,
            PrimaryImageUrlLoader primaryImageUrlLoader,
            UrlRedirectService urlRedirectService,
            SeoUrlService seoUrlService,
            SitemapService sitemapService
    ) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productFeatureRepository = productFeatureRepository;
        this.categoryRepository = categoryRepository;
        this.auditService = auditService;
        this.mediaApplicationService = mediaApplicationService;
        this.primaryImageUrlLoader = primaryImageUrlLoader;
        this.urlRedirectService = urlRedirectService;
        this.seoUrlService = seoUrlService;
        this.sitemapService = sitemapService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminProductSummaryResponse> search(
            ProductSearchCriteria criteria,
            Boolean published,
            int page,
            int size,
            String sort
    ) {
        Page<Product> result = productRepository.findAll(
                adminSpec(criteria, published),
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 48), resolveSort(sort))
        );
        Map<UUID, String> images = primaryImageUrlLoader.load(result.getContent().stream().map(Product::getId).toList());
        List<AdminProductSummaryResponse> content = result.getContent().stream()
                .map(product -> ProductMapper.toAdminSummary(product, images.get(product.getId())))
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
    public ProductAdminResponse getById(UUID id) {
        return toAdmin(requireProduct(id));
    }

    @Transactional
    public ProductAdminResponse create(ProductWriteRequest request) {
        validateWrite(request, null);
        Product product = new Product();
        apply(product, request);
        product = productRepository.save(product);
        replaceChildren(product, request);
        sitemapService.invalidateCache();
        auditService.record(AuditAction.CREATE, "Product", product.getId(), "Producto creado: " + product.getSlug());
        return toAdmin(product);
    }

    @Transactional
    public ProductAdminResponse update(UUID id, ProductWriteRequest request) {
        Product product = requireProduct(id);
        validateWrite(request, id);
        String previousSlug = product.getSlug();
        ProductType previousType = product.getProductType();
        apply(product, request);
        if (!previousSlug.equals(product.getSlug()) || previousType != product.getProductType()) {
            urlRedirectService.redirectSlugChange(
                    seoUrlService.productPath(previousType, previousSlug),
                    seoUrlService.productPath(product)
            );
        }
        product = productRepository.save(product);
        replaceChildren(product, request);
        sitemapService.invalidateCache();
        auditService.record(AuditAction.UPDATE, "Product", product.getId(), "Producto actualizado: " + product.getSlug());
        return toAdmin(product);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = requireProduct(id);
        String slug = product.getSlug();
        List<String> publicIds = productImageRepository.findByProductIdOrderByPositionAsc(id).stream()
                .map(ProductImage::getPublicId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        productImageRepository.findByProductIdOrderByPositionAsc(id)
                .forEach(productImageRepository::delete);
        productFeatureRepository.findByProductIdOrderByNameAsc(id)
                .forEach(productFeatureRepository::delete);
        productImageRepository.flush();
        productFeatureRepository.flush();
        productRepository.delete(product);
        sitemapService.invalidateCache();
        auditService.record(AuditAction.DELETE, "Product", id, "Producto eliminado: " + slug);
        publicIds.forEach(mediaApplicationService::tryDeleteIfUnreferenced);
    }

    @Transactional
    public ProductAdminResponse setPublished(UUID id, boolean published) {
        Product product = requireProduct(id);
        product.setPublished(published);
        if (published && product.getPublishedAt() == null) {
            product.setPublishedAt(Instant.now());
        }
        productRepository.save(product);
        sitemapService.invalidateCache();
        auditService.record(
                published ? AuditAction.PUBLISH : AuditAction.UNPUBLISH,
                "Product",
                id,
                published ? "Producto publicado" : "Producto despublicado"
        );
        return toAdmin(product);
    }

    private void validateWrite(ProductWriteRequest request, UUID currentId) {
        productRepository.findBySlug(request.slug()).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BusinessConflictException("Ya existe un producto con ese slug");
            }
        });

        if (StringUtils.hasText(request.imei())) {
            productRepository.findByImei(request.imei()).ifPresent(existing -> {
                if (currentId == null || !existing.getId().equals(currentId)) {
                    throw new BusinessConflictException("Ya existe un producto con ese IMEI");
                }
            });
        }

        if (request.condition() == ProductCondition.USED
                && request.productType() == ProductType.IPHONE
                && request.batteryHealth() == null) {
            throw new BusinessConflictException("La salud de batería es obligatoria para iPhone usados");
        }

        Product probe = new Product();
        probe.setProductType(request.productType());
        probe.setCondition(request.condition());
        probe.setBatteryHealth(request.batteryHealth());
        probe.setPrice(request.price());
        probe.setPreviousPrice(request.previousPrice());
        ProductRules.validateAll(probe);

        List<ProductImage> images = new ArrayList<>();
        if (request.images() != null) {
            for (ProductImageWriteRequest imageRequest : request.images()) {
                ProductImage image = new ProductImage();
                image.setMainImage(imageRequest.mainImage());
                images.add(image);
            }
        }
        ProductImageRules.validateSingleMainImage(images);
    }

    private void apply(Product product, ProductWriteRequest request) {
        product.setName(request.name().trim());
        product.setSlug(request.slug().trim().toLowerCase(Locale.ROOT));
        product.setModel(blankToNull(request.model()));
        product.setDescription(request.description());
        product.setProductType(request.productType());
        product.setCondition(request.condition());
        product.setStorageCapacity(blankToNull(request.storageCapacity()));
        product.setColor(blankToNull(request.color()));
        product.setBatteryHealth(request.batteryHealth());
        product.setPrice(request.price());
        product.setPreviousPrice(request.previousPrice());
        product.setCurrency(request.currency());
        product.setStock(request.stock());
        product.setWarranty(blankToNull(request.warranty()));
        product.setImei(blankToNull(request.imei()));
        product.setPublished(request.published());
        product.setFeatured(request.featured());
        product.setSeoTitle(blankToNull(request.seoTitle()));
        product.setMetaDescription(blankToNull(request.metaDescription()));
        product.setIndexable(request.indexable() == null || request.indexable());
        if (request.published() && product.getPublishedAt() == null) {
            product.setPublishedAt(Instant.now());
        }
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }
    }

    private void replaceChildren(Product product, ProductWriteRequest request) {
        List<String> previousPublicIds = productImageRepository.findByProductIdOrderByPositionAsc(product.getId()).stream()
                .map(ProductImage::getPublicId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        productFeatureRepository.findByProductIdOrderByNameAsc(product.getId())
                .forEach(productFeatureRepository::delete);
        productImageRepository.findByProductIdOrderByPositionAsc(product.getId())
                .forEach(productImageRepository::delete);
        productFeatureRepository.flush();
        productImageRepository.flush();

        if (request.features() != null) {
            for (ProductFeatureWriteRequest featureRequest : request.features()) {
                ProductFeature feature = new ProductFeature();
                feature.setProduct(product);
                feature.setName(featureRequest.name().trim());
                feature.setValue(featureRequest.value().trim());
                productFeatureRepository.save(feature);
            }
        }
        java.util.Set<String> keptPublicIds = new java.util.HashSet<>();
        if (request.images() != null) {
            for (ProductImageWriteRequest imageRequest : request.images()) {
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setUrl(imageRequest.url().trim());
                image.setPublicId(blankToNull(imageRequest.publicId()));
                image.setAltText(blankToNull(imageRequest.altText()));
                image.setPosition(imageRequest.position());
                image.setMainImage(imageRequest.mainImage());
                image.setFormat(blankToNull(imageRequest.format()));
                image.setSizeBytes(imageRequest.sizeBytes());
                image.setWidth(imageRequest.width());
                image.setHeight(imageRequest.height());
                productImageRepository.save(image);
                if (StringUtils.hasText(image.getPublicId())) {
                    keptPublicIds.add(image.getPublicId());
                }
            }
        }

        previousPublicIds.stream()
                .filter(id -> !keptPublicIds.contains(id))
                .forEach(mediaApplicationService::tryDeleteIfUnreferenced);
    }

    private ProductAdminResponse toAdmin(Product product) {
        return ProductMapper.toAdminResponse(
                product,
                productImageRepository.findByProductIdOrderByPositionAsc(product.getId()),
                productFeatureRepository.findByProductIdOrderByNameAsc(product.getId())
        );
    }

    private Product requireProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    private Specification<Product> adminSpec(ProductSearchCriteria criteria, Boolean published) {
        return (root, query, cb) -> {
            if (query != null && Long.class != query.getResultType()) {
                root.fetch("category", jakarta.persistence.criteria.JoinType.LEFT);
                query.distinct(true);
            }
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (criteria != null) {
                if (criteria.productType() != null) {
                    predicates.add(cb.equal(root.get("productType"), criteria.productType()));
                }
                if (criteria.condition() != null) {
                    predicates.add(cb.equal(root.get("condition"), criteria.condition()));
                }
                if (StringUtils.hasText(criteria.q())) {
                    String pattern = "%" + criteria.q().trim().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("name")), pattern),
                            cb.like(cb.lower(root.get("slug")), pattern),
                            cb.like(cb.lower(root.get("model")), pattern)
                    ));
                }
                if (StringUtils.hasText(criteria.model())) {
                    predicates.add(cb.equal(cb.lower(root.get("model")), criteria.model().trim().toLowerCase()));
                }
            }
            if (published != null) {
                predicates.add(cb.equal(root.get("published"), published));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private static Sort resolveSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "updatedAt");
        }
        return switch (sort.trim().toLowerCase(Locale.ROOT)) {
            case "price,asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price,desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "name,asc" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Direction.DESC, "updatedAt");
        };
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
