package uy.washop.discount.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uy.washop.discount.domain.DiscountCode;
import uy.washop.discount.domain.DiscountType;
import uy.washop.discount.infrastructure.DiscountCodeRedemptionRepository;
import uy.washop.discount.infrastructure.DiscountCodeRepository;
import uy.washop.order.domain.OrderStatus;
import uy.washop.shared.exception.BusinessConflictException;
import uy.washop.shared.exception.ResourceNotFoundException;

@Service
public class DiscountCodeApplicationService {

    static final Set<OrderStatus> INACTIVE_ORDER_STATUSES = EnumSet.of(
            OrderStatus.EXPIRED,
            OrderStatus.CANCELLED,
            OrderStatus.REJECTED
    );

    private final DiscountCodeRepository discountCodeRepository;
    private final DiscountCodeRedemptionRepository redemptionRepository;

    public DiscountCodeApplicationService(
            DiscountCodeRepository discountCodeRepository,
            DiscountCodeRedemptionRepository redemptionRepository
    ) {
        this.discountCodeRepository = discountCodeRepository;
        this.redemptionRepository = redemptionRepository;
    }

    public record AppliedDiscount(DiscountCode code, BigDecimal amount) {
    }

    public AppliedDiscount resolveForCheckout(String rawCode, BigDecimal eligibleSubtotal, String phoneNormalized) {
        DiscountCode code = requireUsableCode(rawCode, eligibleSubtotal, phoneNormalized);
        BigDecimal amount = calculateDiscount(code, eligibleSubtotal);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessConflictException("El código no genera descuento para este pedido");
        }
        return new AppliedDiscount(code, amount);
    }

    public AppliedDiscount preview(String rawCode, BigDecimal eligibleSubtotal) {
        DiscountCode code = requireUsableCode(rawCode, eligibleSubtotal, null);
        BigDecimal amount = calculateDiscount(code, eligibleSubtotal);
        return new AppliedDiscount(code, amount);
    }

    public DiscountCode requireUsableCode(String rawCode, BigDecimal eligibleSubtotal, String phoneNormalized) {
        String normalized = normalizeCode(rawCode);
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessConflictException("Ingresá un código de descuento");
        }
        DiscountCode code = discountCodeRepository.findByCodeIgnoreCase(normalized)
                .orElseThrow(() -> new ResourceNotFoundException("Código de descuento no válido"));

        if (!code.isActive()) {
            throw new BusinessConflictException("Este código ya no está activo");
        }

        Instant now = Instant.now();
        if (code.getStartsAt() != null && now.isBefore(code.getStartsAt())) {
            throw new BusinessConflictException("Este código todavía no está vigente");
        }
        if (code.getEndsAt() != null && now.isAfter(code.getEndsAt())) {
            throw new BusinessConflictException("Este código expiró");
        }

        if (code.getDiscountType() == DiscountType.PERCENT
                && code.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessConflictException("Código de descuento inválido");
        }

        long activeRedemptions = redemptionRepository.countActiveByCode(code.getId(), INACTIVE_ORDER_STATUSES);
        if (code.getMaxUses() != null && activeRedemptions >= code.getMaxUses()) {
            throw new BusinessConflictException("Este código alcanzó el máximo de usos");
        }

        if (StringUtils.hasText(phoneNormalized)
                && redemptionRepository.existsActiveByCodeAndPhone(
                        code.getId(), phoneNormalized, INACTIVE_ORDER_STATUSES)) {
            throw new BusinessConflictException("Este código ya fue usado con este teléfono");
        }

        if (eligibleSubtotal == null || eligibleSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessConflictException("No hay monto aplicable para el descuento");
        }

        return code;
    }

    public static BigDecimal calculateDiscount(DiscountCode code, BigDecimal eligibleSubtotal) {
        BigDecimal subtotal = eligibleSubtotal.max(BigDecimal.ZERO);
        BigDecimal amount;
        if (code.getDiscountType() == DiscountType.PERCENT) {
            amount = subtotal
                    .multiply(code.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            amount = code.getDiscountValue().min(subtotal).setScale(2, RoundingMode.HALF_UP);
        }
        return amount.max(BigDecimal.ZERO);
    }

    public static String normalizeCode(String rawCode) {
        if (!StringUtils.hasText(rawCode)) {
            return "";
        }
        return rawCode.trim().toUpperCase();
    }
}
