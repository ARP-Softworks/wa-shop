package uy.washop.technicalservice.application;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.seo.application.SeoUrlService;
import uy.washop.seo.application.SitemapService;
import uy.washop.seo.application.UrlRedirectService;
import uy.washop.shared.exception.BusinessConflictException;
import uy.washop.shared.exception.ResourceNotFoundException;
import uy.washop.technicalservice.api.dto.TechnicalServiceResponse;
import uy.washop.technicalservice.api.dto.TechnicalServiceWriteRequest;
import uy.washop.technicalservice.api.mapper.TechnicalServiceMapper;
import uy.washop.technicalservice.domain.TechnicalService;
import uy.washop.technicalservice.infrastructure.TechnicalServiceRepository;

@Service
public class AdminTechnicalServiceService {

    private final TechnicalServiceRepository technicalServiceRepository;
    private final AuditService auditService;
    private final UrlRedirectService urlRedirectService;
    private final SeoUrlService seoUrlService;
    private final SitemapService sitemapService;

    public AdminTechnicalServiceService(
            TechnicalServiceRepository technicalServiceRepository,
            AuditService auditService,
            UrlRedirectService urlRedirectService,
            SeoUrlService seoUrlService,
            SitemapService sitemapService
    ) {
        this.technicalServiceRepository = technicalServiceRepository;
        this.auditService = auditService;
        this.urlRedirectService = urlRedirectService;
        this.seoUrlService = seoUrlService;
        this.sitemapService = sitemapService;
    }

    @Transactional(readOnly = true)
    public List<TechnicalServiceResponse> listAll() {
        return technicalServiceRepository.findAll().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(TechnicalServiceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TechnicalServiceResponse getById(UUID id) {
        return TechnicalServiceMapper.toResponse(require(id));
    }

    @Transactional
    public TechnicalServiceResponse create(TechnicalServiceWriteRequest request) {
        validateSlug(request.slug(), null);
        TechnicalService service = new TechnicalService();
        apply(service, request);
        service = technicalServiceRepository.save(service);
        sitemapService.invalidateCache();
        auditService.record(AuditAction.CREATE, "TechnicalService", service.getId(), "Servicio creado: " + service.getSlug());
        return TechnicalServiceMapper.toResponse(service);
    }

    @Transactional
    public TechnicalServiceResponse update(UUID id, TechnicalServiceWriteRequest request) {
        TechnicalService service = require(id);
        validateSlug(request.slug(), id);
        String previousSlug = service.getSlug();
        apply(service, request);
        if (!previousSlug.equals(service.getSlug())) {
            urlRedirectService.redirectSlugChange(
                    seoUrlService.technicalServicePath(previousSlug),
                    seoUrlService.technicalServicePath(service.getSlug())
            );
        }
        service = technicalServiceRepository.save(service);
        sitemapService.invalidateCache();
        auditService.record(AuditAction.UPDATE, "TechnicalService", id, "Servicio actualizado: " + service.getSlug());
        return TechnicalServiceMapper.toResponse(service);
    }

    @Transactional
    public void delete(UUID id) {
        TechnicalService service = require(id);
        String slug = service.getSlug();
        technicalServiceRepository.delete(service);
        sitemapService.invalidateCache();
        auditService.record(AuditAction.DELETE, "TechnicalService", id, "Servicio eliminado: " + slug);
    }

    private void apply(TechnicalService service, TechnicalServiceWriteRequest request) {
        service.setName(request.name().trim());
        service.setSlug(request.slug().trim().toLowerCase(Locale.ROOT));
        service.setDescription(StringUtils.hasText(request.description()) ? request.description().trim() : null);
        service.setPrice(request.price());
        service.setCurrency(request.currency());
        service.setEstimatedTime(StringUtils.hasText(request.estimatedTime()) ? request.estimatedTime().trim() : null);
        service.setActive(request.active());
        service.setSeoTitle(StringUtils.hasText(request.seoTitle()) ? request.seoTitle().trim() : null);
        service.setMetaDescription(StringUtils.hasText(request.metaDescription()) ? request.metaDescription().trim() : null);
        service.setIndexable(request.indexable() == null || request.indexable());
    }

    private void validateSlug(String slug, UUID currentId) {
        technicalServiceRepository.findBySlug(slug.trim().toLowerCase(Locale.ROOT)).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BusinessConflictException("Ya existe un servicio con ese slug");
            }
        });
    }

    private TechnicalService require(UUID id) {
        return technicalServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio técnico no encontrado"));
    }
}
