package uy.washop.media.application;

import java.io.ByteArrayInputStream;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.media.api.dto.MediaUploadResponse;
import uy.washop.media.application.MediaFileValidator.ValidatedMediaFile;
import uy.washop.media.domain.StoredMedia;
import uy.washop.banner.infrastructure.HeroBannerRepository;
import uy.washop.product.infrastructure.ProductImageRepository;
import uy.washop.settings.infrastructure.SiteSettingsRepository;
import uy.washop.shared.exception.BusinessConflictException;

@Service
public class MediaApplicationService {

    private static final Logger log = LoggerFactory.getLogger(MediaApplicationService.class);

    private final MediaStorageService mediaStorageService;
    private final MediaFileValidator mediaFileValidator;
    private final ProductImageRepository productImageRepository;
    private final SiteSettingsRepository siteSettingsRepository;
    private final HeroBannerRepository heroBannerRepository;
    private final AuditService auditService;

    public MediaApplicationService(
            MediaStorageService mediaStorageService,
            MediaFileValidator mediaFileValidator,
            ProductImageRepository productImageRepository,
            SiteSettingsRepository siteSettingsRepository,
            HeroBannerRepository heroBannerRepository,
            AuditService auditService
    ) {
        this.mediaStorageService = mediaStorageService;
        this.mediaFileValidator = mediaFileValidator;
        this.productImageRepository = productImageRepository;
        this.siteSettingsRepository = siteSettingsRepository;
        this.heroBannerRepository = heroBannerRepository;
        this.auditService = auditService;
    }

    @Transactional
    public MediaUploadResponse upload(MultipartFile file, String folder) {
        ValidatedMediaFile validated = mediaFileValidator.validate(file);
        String targetFolder = sanitizeFolder(folder);
        try {
            StoredMedia stored = mediaStorageService.store(
                    new ByteArrayInputStream(validated.bytes()),
                    validated.bytes().length,
                    validated.type().contentType(),
                    validated.safeFilename(),
                    targetFolder
            );
            Integer width = stored.width() != null ? stored.width() : validated.width();
            Integer height = stored.height() != null ? stored.height() : validated.height();
            auditService.record(
                    AuditAction.CREATE,
                    "Media",
                    null,
                    "Imagen subida (" + mediaStorageService.providerName() + "): " + stored.publicId()
            );
            return new MediaUploadResponse(
                    stored.url(),
                    stored.publicId(),
                    mediaStorageService.providerName(),
                    stored.format() != null ? stored.format() : validated.type().extension(),
                    stored.contentType() != null ? stored.contentType() : validated.type().contentType(),
                    stored.sizeBytes() > 0 ? stored.sizeBytes() : validated.bytes().length,
                    width,
                    height
            );
        } catch (BusinessConflictException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Media upload failed for folder={} provider={}", targetFolder, mediaStorageService.providerName(), ex);
            throw new BusinessConflictException("No se pudo subir la imagen. Reintentá más tarde.");
        }
    }

    @Transactional
    public void deleteIfUnreferenced(String publicId) {
        if (!StringUtils.hasText(publicId)) {
            throw new BusinessConflictException("publicId es obligatorio");
        }
        String id = publicId.trim();
        if (isReferenced(id)) {
            throw new BusinessConflictException(
                    "La imagen todavía está referenciada y no puede eliminarse del almacenamiento"
            );
        }
        try {
            mediaStorageService.delete(id);
            auditService.record(AuditAction.DELETE, "Media", null, "Imagen eliminada del almacenamiento: " + id);
        } catch (BusinessConflictException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Media delete failed for publicId={} provider={}", id, mediaStorageService.providerName(), ex);
            throw new BusinessConflictException("No se pudo eliminar la imagen del almacenamiento");
        }
    }

    /**
     * Best-effort cleanup after product/settings updates. Never fails the parent transaction.
     */
    public void tryDeleteIfUnreferenced(String publicId) {
        if (!StringUtils.hasText(publicId) || isReferenced(publicId.trim())) {
            return;
        }
        try {
            mediaStorageService.delete(publicId.trim());
            auditService.record(
                    AuditAction.DELETE,
                    "Media",
                    null,
                    "Imagen huérfana eliminada: " + publicId.trim()
            );
        } catch (Exception ex) {
            log.warn(
                    "Could not delete unreferenced media publicId={} provider={}",
                    publicId,
                    mediaStorageService.providerName()
            );
        }
    }

    public boolean isReferenced(String publicId) {
        return productImageRepository.existsByPublicId(publicId)
                || siteSettingsRepository.existsByLogoPublicId(publicId)
                || heroBannerRepository.existsByImagePublicId(publicId);
    }

    private static String sanitizeFolder(String folder) {
        if (!StringUtils.hasText(folder)) {
            return "uploads";
        }
        String cleaned = folder.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/_-]", "");
        cleaned = cleaned.replaceAll("^/+", "").replaceAll("/+$", "");
        return StringUtils.hasText(cleaned) ? cleaned : "uploads";
    }
}
