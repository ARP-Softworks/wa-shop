package uy.washop.order.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal
) {
}
