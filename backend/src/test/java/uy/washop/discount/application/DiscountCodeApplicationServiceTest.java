package uy.washop.discount.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uy.washop.discount.domain.DiscountCode;
import uy.washop.discount.domain.DiscountType;
import uy.washop.discount.infrastructure.DiscountCodeRedemptionRepository;
import uy.washop.discount.infrastructure.DiscountCodeRepository;
import uy.washop.shared.exception.BusinessConflictException;
import uy.washop.shared.exception.ResourceNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscountCodeApplicationServiceTest {

    @Mock private DiscountCodeRepository discountCodeRepository;
    @Mock private DiscountCodeRedemptionRepository redemptionRepository;

    private DiscountCodeApplicationService service;

    @BeforeEach
    void setUp() {
        service = new DiscountCodeApplicationService(discountCodeRepository, redemptionRepository);
    }

    @Test
    void appliesPercentDiscount() {
        DiscountCode code = activeCode("SAVE10", DiscountType.PERCENT, new BigDecimal("10"));
        when(discountCodeRepository.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.of(code));
        when(redemptionRepository.countActiveByCode(eq(code.getId()), any())).thenReturn(0L);

        var applied = service.preview("save10", new BigDecimal("1000.00"));
        assertThat(applied.amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void appliesFixedDiscountCappedBySubtotal() {
        DiscountCode code = activeCode("FLAT500", DiscountType.FIXED, new BigDecimal("500"));
        when(discountCodeRepository.findByCodeIgnoreCase("FLAT500")).thenReturn(Optional.of(code));
        when(redemptionRepository.countActiveByCode(eq(code.getId()), any())).thenReturn(0L);

        var applied = service.preview("FLAT500", new BigDecimal("200.00"));
        assertThat(applied.amount()).isEqualByComparingTo("200.00");
    }

    @Test
    void rejectsExpiredCode() {
        DiscountCode code = activeCode("OLD", DiscountType.PERCENT, new BigDecimal("10"));
        code.setEndsAt(Instant.now().minus(1, ChronoUnit.DAYS));
        when(discountCodeRepository.findByCodeIgnoreCase("OLD")).thenReturn(Optional.of(code));

        assertThatThrownBy(() -> service.preview("OLD", new BigDecimal("100")))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("expiró");
    }

    @Test
    void rejectsWhenMaxUsesReached() {
        DiscountCode code = activeCode("ONCE", DiscountType.FIXED, new BigDecimal("50"));
        code.setMaxUses(1);
        when(discountCodeRepository.findByCodeIgnoreCase("ONCE")).thenReturn(Optional.of(code));
        when(redemptionRepository.countActiveByCode(eq(code.getId()), any())).thenReturn(1L);

        assertThatThrownBy(() -> service.preview("ONCE", new BigDecimal("100")))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("máximo");
    }

    @Test
    void rejectsSecondUseSamePhone() {
        DiscountCode code = activeCode("PHONE1", DiscountType.PERCENT, new BigDecimal("5"));
        when(discountCodeRepository.findByCodeIgnoreCase("PHONE1")).thenReturn(Optional.of(code));
        when(redemptionRepository.countActiveByCode(eq(code.getId()), any())).thenReturn(0L);
        when(redemptionRepository.existsActiveByCodeAndPhone(eq(code.getId()), eq("59899111222"), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.resolveForCheckout("PHONE1", new BigDecimal("100"), "59899111222"))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("teléfono");
    }

    @Test
    void rejectsUnknownCode() {
        when(discountCodeRepository.findByCodeIgnoreCase("NOPE")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.preview("NOPE", new BigDecimal("100")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static DiscountCode activeCode(String value, DiscountType type, BigDecimal amount) {
        DiscountCode code = new DiscountCode();
        code.setId(UUID.randomUUID());
        code.setCode(value);
        code.setActive(true);
        code.setDiscountType(type);
        code.setDiscountValue(amount);
        return code;
    }
}
