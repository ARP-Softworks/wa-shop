package uy.washop.order.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderCreateRequest(
        @NotBlank @Size(max = 200) String customerName,
        @NotBlank @Size(max = 40) String customerPhone,
        @Email @Size(max = 320) String customerEmail,
        @Size(max = 2000) String shippingAddress,
        @NotEmpty @Valid List<OrderItemRequest> items
) {
}
