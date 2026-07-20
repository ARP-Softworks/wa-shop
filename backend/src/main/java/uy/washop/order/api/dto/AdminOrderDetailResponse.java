package uy.washop.order.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uy.washop.order.domain.OrderStatus;
import uy.washop.shared.domain.CurrencyCode;

public record AdminOrderDetailResponse(
        UUID id,
        UUID customerId,
        String customerName,
        String customerPhone,
        String customerEmail,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal promotionDiscount,
        String discountCode,
        BigDecimal couponDiscount,
        BigDecimal total,
        CurrencyCode currency,
        String shippingAddress,
        String mpPreferenceId,
        String mpPaymentId,
        String mpPaymentStatus,
        String notes,
        List<OrderItemResponse> items,
        List<OrderStatusHistoryResponse> statusHistory,
        Instant createdAt,
        Instant updatedAt
) {
}
