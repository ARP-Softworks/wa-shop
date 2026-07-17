package uy.washop.order.api;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.order.api.dto.OrderCreateRequest;
import uy.washop.order.api.dto.OrderCreateResponse;
import uy.washop.order.api.dto.OrderStatusResponse;
import uy.washop.order.application.CheckoutService;

@RestController
@RequestMapping("/api/public/orders")
public class PublicOrderController {

    private final CheckoutService checkoutService;

    public PublicOrderController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderCreateResponse checkout(@Valid @RequestBody OrderCreateRequest request) {
        return checkoutService.checkout(request);
    }

    @GetMapping("/{id}/status")
    public OrderStatusResponse getStatus(@PathVariable UUID id) {
        return checkoutService.getStatus(id);
    }
}
