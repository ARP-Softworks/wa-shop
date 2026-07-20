package uy.washop.product.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import uy.washop.product.api.dto.ProductVariantWriteRequest;
import uy.washop.product.api.dto.ProductWriteRequest;
import uy.washop.product.api.mapper.ProductMapper;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCompatibleModel;
import uy.washop.product.domain.ProductFeature;
import uy.washop.product.domain.ProductImage;
import uy.washop.product.domain.ProductImageRules;
import uy.washop.product.domain.ProductRules;
import uy.washop.product.domain.ProductType;
import uy.washop.product.domain.ProductVariant;
import uy.washop.product.infrastructure.ProductCompatibleModelRepository;
import uy.washop.product.infrastructure.ProductFeatureRepository;
import uy.washop.product.infrastructure.ProductImageRepository;
import uy.washop.product.infrastructure.ProductRepository;
import uy.washop.product.infrastructure.ProductVariantRepository;
import uy.washop.seo.application.SeoUrlService;
import uy.washop.seo.application.SitemapService;
import uy.washop.seo.application.UrlRedirectService;
import uy.washop.shared.api.PageResponse;
import uy.washop.shared.exception.BusinessConflictException;
import uy.washop.shared.exception.ResourceNotFoundException;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductFeatureRepository productFeatureRepository;
    private final ProductCompatibleModelRepository productCompatibleModelRepository;
    private final CategoryRepository categoryRepository;
    private final AuditService auditService;
    private final MediaApplicationService mediaApplicationService;
    private final PrimaryImageUrlLoader primaryImageUrlLoader;
    private final UrlRedirectService urlRedirectService;
    private final SeoUrlService seoUrlService;
    private final SitemapService sitemapService;

    public AdminProductService(
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository,
            ProductImageRepository productImageRepository,
            ProductFeatureRepository productFeatureRepository,
            ProductCompatibleModelRepository productCompatibleModelRepository,
            CategoryRepository categoryRepository,
            AuditService auditService,
            MediaApplicationService mediaApplicationService,
            PrimaryImageUrlLoader primaryImageUrlLoader,
            UrlRedirectService urlRedirectService,
            SeoUrlService seoUrlService,
            SitemapService sitemapService
    ) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.productImageRepository = productImageRepository;
        this.productFeatureRepository = productFeatureRepository;
        this.productCompatibleModelRepository = productCompatibleModelRepository;
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
        List<UUID> productIds = result.getContent().stream().map(Product::getId).toList();
        Map<UUID, String> images = primaryImageUrlLoader.load(productIds);
        Map<UUID, Long> variantCounts = productIds.isEmpty()
                ? Map.of()
                : productVariantRepository.findByProduct_IdIn(productIds).stream()
                        .collect(Collectors.groupingBy(v -> v.getProduct().getId(), Collectors.counting()));
        List<AdminProductSummaryResponse> content = result.getContent().stream()
                .map(product -> ProductMapper.toAdminSummary(
                        product,
                        images.get(product.getId()),
                        variantCounts.getOrDefault(product.getId(), 0L).intValue()
                ))
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
        applyParent(product, request);
        seedListingFieldsFromVariants(product, request.variants());
        product = productRepository.save(product);
        replaceFeaturesAndCompatible(product, request);
        syncVariants(product, request.variants());
        syncParentListingFields(product);
        productRepository.save(product);
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
        applyParent(product, request);
        if (!previousSlug.equals(product.getSlug()) || previousType != product.getProductType()) {
            urlRedirectService.redirectSlugChange(
                    seoUrlService.productPath(previousType, previousSlug),
                    seoUrlService.productPath(product)
            );
        }
        product = productRepository.save(product);
        replaceFeaturesAndCompatible(product, request);
        syncVariants(product, request.variants());
        syncParentListingFields(product);
        productRepository.save(product);
        sitemapService.invalidateCache();
        auditService.record(AuditAction.UPDATE, "Product", product.getId(), "Producto actualizado: " + product.getSlug());
        return toAdmin(product);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = requireProduct(id);
        String slug = product.getSlug();
        List<ProductVariant> variants = productVariantRepository.findByProduct_IdOrderByPriceAsc(id);
        List<String> publicIds = new ArrayList<>();
        for (ProductVariant variant : variants) {
            productImageRepository.findByVariantIdOrderByPositionAsc(variant.getId()).forEach(image -> {
                if (StringUtils.hasText(image.getPublicId())) {
                    publicIds.add(image.getPublicId());
                }
                productImageRepository.delete(image);
            });
            productVariantRepository.delete(variant);
        }
        productImageRepository.findByProductIdOrderByPositionAsc(id).forEach(image -> {
            if (StringUtils.hasText(image.getPublicId())) {
                publicIds.add(image.getPublicId());
            }
            productImageRepository.delete(image);
        });
        productFeatureRepository.findByProductIdOrderByNameAsc(id)
                .forEach(productFeatureRepository::delete);
        productCompatibleModelRepository.findByProductIdOrderByModelAsc(id)
                .forEach(productCompatibleModelRepository::delete);
        productImageRepository.flush();
        productVariantRepository.flush();
        productFeatureRepository.flush();
        productCompatibleModelRepository.flush();
        productRepository.delete(product);
        sitemapService.invalidateCache();
        auditService.record(AuditAction.DELETE, "Product", id, "Producto eliminado: " + slug);
        publicIds.stream().distinct().forEach(mediaApplicationService::tryDeleteIfUnreferenced);
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

        ProductRules.validatePromotion(request.promoBuyQuantity(), request.promoPayQuantity());

        Set<UUID> seenVariantIds = new HashSet<>();
        for (ProductVariantWriteRequest variant : request.variants()) {
            ProductRules.validateVariant(
                    request.productType(),
                    variant.condition(),
                    variant.batteryHealth(),
                    variant.price(),
                    variant.previousPrice()
            );
            if (variant.id() != null) {
                if (!seenVariantIds.add(variant.id())) {
                    throw new BusinessConflictException("Hay variantes duplicadas en la solicitud");
                }
                ProductVariant existing = productVariantRepository.findById(variant.id())
                        .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada"));
                if (currentId == null || !existing.getProduct().getId().equals(currentId)) {
                    throw new BusinessConflictException("La variante no pertenece a este producto");
                }
            }
            if (StringUtils.hasText(variant.imei())) {
                boolean conflict = variant.id() == null
                        ? productVariantRepository.existsByImei(variant.imei())
                        : productVariantRepository.existsByImeiAndIdNot(variant.imei(), variant.id());
                if (conflict) {
                    throw new BusinessConflictException("Ya existe una variante con ese IMEI");
                }
            }
            List<ProductImage> images = new ArrayList<>();
            if (variant.images() != null) {
                for (ProductImageWriteRequest imageRequest : variant.images()) {
                    ProductImage image = new ProductImage();
                    image.setMainImage(imageRequest.mainImage());
                    images.add(image);
                }
            }
            ProductImageRules.validateSingleMainImage(images);
        }
    }

    private void applyParent(Product product, ProductWriteRequest request) {
        product.setName(request.name().trim());
        product.setSlug(request.slug().trim().toLowerCase(Locale.ROOT));
        product.setModel(blankToNull(request.model()));
        product.setDescription(request.description());
        product.setProductType(request.productType());
        product.setPromoBuyQuantity(request.promoBuyQuantity());
        product.setPromoPayQuantity(request.promoPayQuantity());
        product.setPublished(request.published());
        product.setFeatured(request.featured());
        product.setSeoTitle(blankToNull(request.seoTitle()));
        product.setMetaDescription(blankToNull(request.metaDescription()));
        product.setIndexable(request.indexable() == null || request.indexable());
        product.setProductGroup(null);
        product.setImei(null);
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

    private void syncVariants(Product product, List<ProductVariantWriteRequest> requests) {
        List<ProductVariant> existing = productVariantRepository.findByProduct_IdOrderByPriceAsc(product.getId());
        Map<UUID, ProductVariant> existingById = existing.stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));
        Set<UUID> keptIds = new HashSet<>();
        List<String> previousPublicIds = new ArrayList<>();
        for (ProductVariant variant : existing) {
            productImageRepository.findByVariantIdOrderByPositionAsc(variant.getId()).forEach(image -> {
                if (StringUtils.hasText(image.getPublicId())) {
                    previousPublicIds.add(image.getPublicId());
                }
            });
        }

        Set<String> keptPublicIds = new HashSet<>();
        for (ProductVariantWriteRequest request : requests) {
            ProductVariant variant;
            if (request.id() != null) {
                variant = existingById.get(request.id());
                if (variant == null) {
                    throw new ResourceNotFoundException("Variante no encontrada");
                }
                keptIds.add(request.id());
            } else {
                variant = new ProductVariant();
                variant.setProduct(product);
            }
            applyVariant(variant, request);
            variant = productVariantRepository.save(variant);
            keptIds.add(variant.getId());
            keptPublicIds.addAll(replaceVariantImages(product, variant, request.images()));
        }

        for (ProductVariant variant : existing) {
            if (!keptIds.contains(variant.getId())) {
                productImageRepository.findByVariantIdOrderByPositionAsc(variant.getId())
                        .forEach(productImageRepository::delete);
                productVariantRepository.delete(variant);
            }
        }
        productImageRepository.flush();
        productVariantRepository.flush();

        previousPublicIds.stream()
                .filter(id -> !keptPublicIds.contains(id))
                .distinct()
                .forEach(mediaApplicationService::tryDeleteIfUnreferenced);
    }

    private void applyVariant(ProductVariant variant, ProductVariantWriteRequest request) {
        variant.setCondition(request.condition());
        variant.setStorageCapacity(blankToNull(request.storageCapacity()));
        variant.setColor(blankToNull(request.color()));
        variant.setBatteryHealth(request.batteryHealth());
        variant.setPrice(request.price());
        variant.setPreviousPrice(request.previousPrice());
        variant.setCurrency(request.currency());
        variant.setStock(request.stock());
        variant.setWarranty(blankToNull(request.warranty()));
        variant.setImei(blankToNull(request.imei()));
        variant.setPublished(request.published());
    }

    private Set<String> replaceVariantImages(
            Product product,
            ProductVariant variant,
            List<ProductImageWriteRequest> images
    ) {
        productImageRepository.findByVariantIdOrderByPositionAsc(variant.getId())
                .forEach(productImageRepository::delete);
        productImageRepository.flush();
        Set<String> keptPublicIds = new HashSet<>();
        if (images == null) {
            return keptPublicIds;
        }
        for (ProductImageWriteRequest imageRequest : images) {
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setVariant(variant);
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
        return keptPublicIds;
    }

    private void seedListingFieldsFromVariants(Product product, List<ProductVariantWriteRequest> variants) {
        ProductVariantWriteRequest representative = variants.stream()
                .min(Comparator
                        .comparing((ProductVariantWriteRequest v) -> !v.published())
                        .thenComparing(ProductVariantWriteRequest::price))
                .orElseThrow();
        product.setCondition(representative.condition());
        product.setStorageCapacity(blankToNull(representative.storageCapacity()));
        product.setColor(blankToNull(representative.color()));
        product.setBatteryHealth(representative.batteryHealth());
        product.setPrice(representative.price());
        product.setPreviousPrice(representative.previousPrice());
        product.setCurrency(representative.currency());
        product.setWarranty(blankToNull(representative.warranty()));
        product.setStock(variants.stream().mapToInt(ProductVariantWriteRequest::stock).sum());
        product.setImei(null);
    }

    private void syncParentListingFields(Product product) {
        List<ProductVariant> variants = productVariantRepository.findByProduct_IdOrderByPriceAsc(product.getId());
        if (variants.isEmpty()) {
            throw new BusinessConflictException("El producto debe tener al menos una variante");
        }
        ProductVariant representative = variants.stream()
                .min(Comparator
                        .comparing((ProductVariant v) -> !v.isPublished())
                        .thenComparing(ProductVariant::getPrice)
                        .thenComparing(ProductVariant::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow();
        product.setCondition(representative.getCondition());
        product.setStorageCapacity(representative.getStorageCapacity());
        product.setColor(representative.getColor());
        product.setBatteryHealth(representative.getBatteryHealth());
        product.setPrice(representative.getPrice());
        product.setPreviousPrice(representative.getPreviousPrice());
        product.setCurrency(representative.getCurrency());
        product.setWarranty(representative.getWarranty());
        product.setStock(variants.stream().mapToInt(ProductVariant::getStock).sum());
        product.setImei(null);
    }

    private void replaceFeaturesAndCompatible(Product product, ProductWriteRequest request) {
        productFeatureRepository.findByProductIdOrderByNameAsc(product.getId())
                .forEach(productFeatureRepository::delete);
        productCompatibleModelRepository.findByProductIdOrderByModelAsc(product.getId())
                .forEach(productCompatibleModelRepository::delete);
        productFeatureRepository.flush();
        productCompatibleModelRepository.flush();

        if (request.compatibleModels() != null) {
            for (String model : request.compatibleModels()) {
                if (!StringUtils.hasText(model)) {
                    continue;
                }
                ProductCompatibleModel compatibleModel = new ProductCompatibleModel();
                compatibleModel.setProduct(product);
                compatibleModel.setModel(model.trim());
                productCompatibleModelRepository.save(compatibleModel);
            }
        }

        if (request.features() != null) {
            for (ProductFeatureWriteRequest featureRequest : request.features()) {
                ProductFeature feature = new ProductFeature();
                feature.setProduct(product);
                feature.setName(featureRequest.name().trim());
                feature.setValue(featureRequest.value().trim());
                productFeatureRepository.save(feature);
            }
        }
    }

    private ProductAdminResponse toAdmin(Product product) {
        List<ProductVariant> variants = productVariantRepository.findByProduct_IdOrderByPriceAsc(product.getId());
        Map<UUID, List<ProductImage>> imagesByVariant = loadImagesByVariant(variants);
        return ProductMapper.toAdminResponse(
                product,
                variants,
                imagesByVariant,
                productFeatureRepository.findByProductIdOrderByNameAsc(product.getId()),
                productCompatibleModelRepository.findByProductIdOrderByModelAsc(product.getId()).stream()
                        .map(ProductCompatibleModel::getModel)
                        .toList()
        );
    }

    private Map<UUID, List<ProductImage>> loadImagesByVariant(List<ProductVariant> variants) {
        if (variants.isEmpty()) {
            return Map.of();
        }
        List<UUID> variantIds = variants.stream().map(ProductVariant::getId).toList();
        Map<UUID, List<ProductImage>> map = new HashMap<>();
        for (ProductImage image : productImageRepository.findByVariant_IdInOrderByPositionAsc(variantIds)) {
            if (image.getVariant() == null) {
                continue;
            }
            map.computeIfAbsent(image.getVariant().getId(), ignored -> new ArrayList<>()).add(image);
        }
        return map;
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
