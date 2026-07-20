package uy.washop.discount.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.discount.api.dto.DiscountCodeResponse;
import uy.washop.discount.api.dto.DiscountCodeWriteRequest;
import uy.washop.discount.api.mapper.DiscountCodeMapper;
import uy.washop.discount.domain.DiscountCode;
import uy.washop.discount.domain.DiscountType;
import uy.washop.discount.infrastructure.DiscountCodeRedemptionRepository;
import uy.washop.discount.infrastructure.DiscountCodeRepository;
import uy.washop.shared.exception.BusinessConflictException;
import uy.washop.shared.exception.ResourceNotFoundException;

@Service
public class AdminDiscountCodeService {

    private final DiscountCodeRepository discountCodeRepository;
    private final DiscountCodeRedemptionRepository redemptionRepository;
    private final AuditService auditService;

    public AdminDiscountCodeService(
            DiscountCodeRepository discountCodeRepository,
            DiscountCodeRedemptionRepository redemptionRepository,
            AuditService auditService
    ) {
        this.discountCodeRepository = discountCodeRepository;
        this.redemptionRepository = redemptionRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<DiscountCodeResponse> listAll() {
        return discountCodeRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(entity -> DiscountCodeMapper.toResponse(entity, usedCount(entity.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public DiscountCodeResponse getById(UUID id) {
        DiscountCode entity = require(id);
        return DiscountCodeMapper.toResponse(entity, usedCount(id));
    }

    @Transactional
    public DiscountCodeResponse create(DiscountCodeWriteRequest request) {
        String code = DiscountCodeApplicationService.normalizeCode(request.code());
        if (discountCodeRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessConflictException("Ya existe un código con ese valor");
        }
        validateWrite(request);

        DiscountCode entity = new DiscountCode();
        apply(entity, request, code);
        entity = discountCodeRepository.save(entity);
        auditService.record(AuditAction.CREATE, "DiscountCode", entity.getId(), "Código " + entity.getCode());
        return DiscountCodeMapper.toResponse(entity, 0);
    }

    @Transactional
    public DiscountCodeResponse update(UUID id, DiscountCodeWriteRequest request) {
        DiscountCode entity = require(id);
        String code = DiscountCodeApplicationService.normalizeCode(request.code());
        if (discountCodeRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BusinessConflictException("Ya existe un código con ese valor");
        }
        validateWrite(request);
        apply(entity, request, code);
        entity = discountCodeRepository.save(entity);
        auditService.record(AuditAction.UPDATE, "DiscountCode", entity.getId(), "Código " + entity.getCode());
        return DiscountCodeMapper.toResponse(entity, usedCount(id));
    }

    private long usedCount(UUID codeId) {
        return redemptionRepository.countActiveByCode(codeId, DiscountCodeApplicationService.INACTIVE_ORDER_STATUSES);
    }

    @Transactional
    public void delete(UUID id) {
        DiscountCode entity = require(id);
        discountCodeRepository.delete(entity);
        auditService.record(AuditAction.DELETE, "DiscountCode", id, "Código " + entity.getCode());
    }

    private DiscountCode require(UUID id) {
        return discountCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Código de descuento no encontrado"));
    }

    private static void validateWrite(DiscountCodeWriteRequest request) {
        if (request.discountType() == DiscountType.PERCENT
                && request.discountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessConflictException("El porcentaje no puede superar 100");
        }
        Instant starts = request.startsAt();
        Instant ends = request.endsAt();
        if (starts != null && ends != null && starts.isAfter(ends)) {
            throw new BusinessConflictException("La fecha de inicio no puede ser posterior a la de fin");
        }
        if (request.maxUses() != null && request.maxUses() < 1) {
            throw new BusinessConflictException("El máximo de usos debe ser al menos 1");
        }
    }

    private static void apply(DiscountCode entity, DiscountCodeWriteRequest request, String code) {
        entity.setCode(code);
        entity.setActive(request.active());
        entity.setDiscountType(request.discountType());
        entity.setDiscountValue(request.discountValue());
        entity.setMaxUses(request.maxUses());
        entity.setStartsAt(request.startsAt());
        entity.setEndsAt(request.endsAt());
    }
}
