package uy.washop.category.application;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.category.api.dto.CategoryResponse;
import uy.washop.category.api.dto.CategoryWriteRequest;
import uy.washop.category.api.mapper.CategoryMapper;
import uy.washop.category.domain.Category;
import uy.washop.category.infrastructure.CategoryRepository;
import uy.washop.seo.application.SeoUrlService;
import uy.washop.seo.application.SitemapService;
import uy.washop.seo.application.UrlRedirectService;
import uy.washop.shared.exception.BusinessConflictException;
import uy.washop.shared.exception.ResourceNotFoundException;

@Service
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditService auditService;
    private final UrlRedirectService urlRedirectService;
    private final SeoUrlService seoUrlService;
    private final SitemapService sitemapService;

    public AdminCategoryService(
            CategoryRepository categoryRepository,
            AuditService auditService,
            UrlRedirectService urlRedirectService,
            SeoUrlService seoUrlService,
            SitemapService sitemapService
    ) {
        this.categoryRepository = categoryRepository;
        this.auditService = auditService;
        this.urlRedirectService = urlRedirectService;
        this.seoUrlService = seoUrlService;
        this.sitemapService = sitemapService;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listAll() {
        return categoryRepository.findAll().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(CategoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID id) {
        return CategoryMapper.toResponse(require(id));
    }

    @Transactional
    public CategoryResponse create(CategoryWriteRequest request) {
        validateSlug(request.slug(), null);
        Category category = new Category();
        apply(category, request);
        category = categoryRepository.save(category);
        sitemapService.invalidateCache();
        auditService.record(AuditAction.CREATE, "Category", category.getId(), "Categoría creada: " + category.getSlug());
        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryWriteRequest request) {
        Category category = require(id);
        validateSlug(request.slug(), id);
        String previousSlug = category.getSlug();
        apply(category, request);
        if (!previousSlug.equals(category.getSlug())) {
            urlRedirectService.redirectSlugChange(
                    seoUrlService.categoryPath(previousSlug),
                    seoUrlService.categoryPath(category.getSlug())
            );
        }
        category = categoryRepository.save(category);
        sitemapService.invalidateCache();
        auditService.record(AuditAction.UPDATE, "Category", id, "Categoría actualizada: " + category.getSlug());
        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public void delete(UUID id) {
        Category category = require(id);
        String slug = category.getSlug();
        categoryRepository.delete(category);
        sitemapService.invalidateCache();
        auditService.record(AuditAction.DELETE, "Category", id, "Categoría eliminada: " + slug);
    }

    private void apply(Category category, CategoryWriteRequest request) {
        category.setName(request.name().trim());
        category.setSlug(request.slug().trim().toLowerCase(Locale.ROOT));
        category.setDescription(StringUtils.hasText(request.description()) ? request.description().trim() : null);
        category.setActive(request.active());
        category.setSeoTitle(StringUtils.hasText(request.seoTitle()) ? request.seoTitle().trim() : null);
        category.setMetaDescription(StringUtils.hasText(request.metaDescription()) ? request.metaDescription().trim() : null);
        category.setIndexable(request.indexable() == null || request.indexable());
    }

    private void validateSlug(String slug, UUID currentId) {
        categoryRepository.findBySlug(slug.trim().toLowerCase(Locale.ROOT)).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BusinessConflictException("Ya existe una categoría con ese slug");
            }
        });
    }

    private Category require(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
    }
}
