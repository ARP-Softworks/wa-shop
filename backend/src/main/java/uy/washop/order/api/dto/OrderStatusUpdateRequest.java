package uy.washop.order.api.dto;

import jakarta.validation.constraints.NotNull;
import uy.washop.order.domain.OrderStatus;

public record OrderStatusUpdateRequest(@NotNull OrderStatus status, String note) {
}
