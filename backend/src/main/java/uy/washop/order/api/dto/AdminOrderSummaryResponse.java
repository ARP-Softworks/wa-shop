package uy.washop.order.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import uy.washop.order.domain.OrderStatus;
import uy.washop.shared.domain.CurrencyCode;

public record AdminOrderSummaryResponse(
        UUID id,
        UUID customerId,
        String customerName,
        String customerPhone,
        OrderStatus status,
        BigDecimal total,
        CurrencyCode currency,
        int itemCount,
        Instant createdAt,
        Instant updatedAt
) {
}
