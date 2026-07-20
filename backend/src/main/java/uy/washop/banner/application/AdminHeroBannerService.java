package uy.washop.banner.application;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.banner.api.dto.HeroBannerResponse;
import uy.washop.banner.api.dto.HeroBannerWriteRequest;
import uy.washop.banner.api.mapper.HeroBannerMapper;
import uy.washop.banner.domain.HeroBanner;
import uy.washop.banner.infrastructure.HeroBannerRepository;
import uy.washop.media.application.MediaApplicationService;
import uy.washop.shared.exception.ResourceNotFoundException;

@Service
public class AdminHeroBannerService {

    private final HeroBannerRepository heroBannerRepository;
    private final AuditService auditService;
    private final MediaApplicationService mediaApplicationService;

    public AdminHeroBannerService(
            HeroBannerRepository heroBannerRepository,
            AuditService auditService,
            MediaApplicationService mediaApplicationService
    ) {
        this.heroBannerRepository = heroBannerRepository;
        this.auditService = auditService;
        this.mediaApplicationService = mediaApplicationService;
    }

    @Transactional(readOnly = true)
    public List<HeroBannerResponse> listAll() {
        return heroBannerRepository.findAllByOrderByPositionAsc().stream()
                .map(HeroBannerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HeroBannerResponse getById(UUID id) {
        return HeroBannerMapper.toResponse(require(id));
    }

    @Transactional
    public HeroBannerResponse create(HeroBannerWriteRequest request) {
        HeroBanner banner = new HeroBanner();
        apply(banner, request);
        banner = heroBannerRepository.save(banner);
        auditService.record(AuditAction.CREATE, "HeroBanner", banner.getId(), "Banner creado");
        return HeroBannerMapper.toResponse(banner);
    }

    @Transactional
    public HeroBannerResponse update(UUID id, HeroBannerWriteRequest request) {
        HeroBanner banner = require(id);
        String previousPublicId = banner.getImagePublicId();
        apply(banner, request);
        banner = heroBannerRepository.save(banner);
        if (StringUtils.hasText(previousPublicId) && !previousPublicId.equals(banner.getImagePublicId())) {
            mediaApplicationService.tryDeleteIfUnreferenced(previousPublicId);
        }
        auditService.record(AuditAction.UPDATE, "HeroBanner", id, "Banner actualizado");
        return HeroBannerMapper.toResponse(banner);
    }

    @Transactional
    public void delete(UUID id) {
        HeroBanner banner = require(id);
        String publicId = banner.getImagePublicId();
        heroBannerRepository.delete(banner);
        auditService.record(AuditAction.DELETE, "HeroBanner", id, "Banner eliminado");
        if (StringUtils.hasText(publicId)) {
            mediaApplicationService.tryDeleteIfUnreferenced(publicId);
        }
    }

    private void apply(HeroBanner banner, HeroBannerWriteRequest request) {
        banner.setImageUrl(request.imageUrl().trim());
        banner.setImagePublicId(StringUtils.hasText(request.imagePublicId()) ? request.imagePublicId().trim() : null);
        banner.setAltText(StringUtils.hasText(request.altText()) ? request.altText().trim() : null);
        banner.setLinkUrl(StringUtils.hasText(request.linkUrl()) ? request.linkUrl().trim() : null);
        banner.setPosition(request.position());
        banner.setActive(request.active());
    }

    private HeroBanner require(UUID id) {
        return heroBannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner no encontrado"));
    }
}
