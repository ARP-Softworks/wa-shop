package uy.washop.promotion.application;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.category.domain.Category;
import uy.washop.category.infrastructure.CategoryRepository;
import uy.washop.promotion.api.dto.PromotionResponse;
import uy.washop.promotion.api.dto.PromotionWriteRequest;
import uy.washop.promotion.api.mapper.PromotionMapper;
import uy.washop.promotion.domain.Promotion;
import uy.washop.promotion.infrastructure.PromotionRepository;
import uy.washop.shared.exception.ResourceNotFoundException;

@Service
public class AdminPromotionService {

    private final PromotionRepository promotionRepository;
    private final CategoryRepository categoryRepository;
    private final AuditService auditService;

    public AdminPromotionService(
            PromotionRepository promotionRepository,
            CategoryRepository categoryRepository,
            AuditService auditService
    ) {
        this.promotionRepository = promotionRepository;
        this.categoryRepository = categoryRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> listAll() {
        return promotionRepository.findAll().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(PromotionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PromotionResponse getById(UUID id) {
        return PromotionMapper.toResponse(require(id));
    }

    @Transactional
    public PromotionResponse create(PromotionWriteRequest request) {
        Promotion promotion = new Promotion();
        apply(promotion, request);
        promotion = promotionRepository.save(promotion);
        auditService.record(AuditAction.CREATE, "Promotion", promotion.getId(), "Promoción creada: " + promotion.getName());
        return PromotionMapper.toResponse(promotion);
    }

    @Transactional
    public PromotionResponse update(UUID id, PromotionWriteRequest request) {
        Promotion promotion = require(id);
        apply(promotion, request);
        promotion = promotionRepository.save(promotion);
        auditService.record(AuditAction.UPDATE, "Promotion", id, "Promoción actualizada: " + promotion.getName());
        return PromotionMapper.toResponse(promotion);
    }

    @Transactional
    public void delete(UUID id) {
        Promotion promotion = require(id);
        String name = promotion.getName();
        promotionRepository.delete(promotion);
        auditService.record(AuditAction.DELETE, "Promotion", id, "Promoción eliminada: " + name);
    }

    private void apply(Promotion promotion, PromotionWriteRequest request) {
        promotion.setName(request.name().trim());
        promotion.setActive(request.active());
        promotion.setTriggerCategory(requireCategory(request.triggerCategoryId()));
        promotion.setTriggerQuantity(request.triggerQuantity());
        promotion.setRewardCategory(requireCategory(request.rewardCategoryId()));
        promotion.setRewardQuantity(request.rewardQuantity());
        promotion.setDiscountPercent(request.discountPercent());
    }

    private Category requireCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
    }

    private Promotion require(UUID id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada"));
    }
}
