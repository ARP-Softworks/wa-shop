package uy.washop.order.api.dto;

import java.util.UUID;
import uy.washop.order.domain.OrderStatus;

/** Public, PII-free — never include customer contact details here. */
public record OrderStatusResponse(UUID orderId, OrderStatus status) {
}
