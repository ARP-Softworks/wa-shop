package uy.washop.order.api.dto;

import java.util.UUID;

public record OrderCreateResponse(UUID orderId, String checkoutUrl) {
}
