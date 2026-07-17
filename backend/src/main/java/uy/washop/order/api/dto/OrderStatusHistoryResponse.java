package uy.washop.order.api.dto;

import java.time.Instant;
import java.util.UUID;
import uy.washop.order.domain.OrderStatus;

public record OrderStatusHistoryResponse(
        UUID id,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        UUID changedBy,
        String note,
        Instant createdAt
) {
}
