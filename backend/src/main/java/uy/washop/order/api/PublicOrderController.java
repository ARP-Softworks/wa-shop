package uy.washop.order.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
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
import uy.washop.order.application.CheckoutRateLimitService;
import uy.washop.order.application.CheckoutService;

@RestController
@RequestMapping("/api/public/orders")
public class PublicOrderController {

    private final CheckoutService checkoutService;
    private final CheckoutRateLimitService checkoutRateLimitService;

    public PublicOrderController(
            CheckoutService checkoutService,
            CheckoutRateLimitService checkoutRateLimitService
    ) {
        this.checkoutService = checkoutService;
        this.checkoutRateLimitService = checkoutRateLimitService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderCreateResponse checkout(
            @Valid @RequestBody OrderCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = resolveClientIp(httpRequest);
        checkoutRateLimitService.assertAllowed(clientIp, request.customerPhone());
        checkoutRateLimitService.recordAttempt(clientIp, request.customerPhone());
        return checkoutService.checkout(request);
    }

    @GetMapping("/{id}/status")
    public OrderStatusResponse getStatus(@PathVariable UUID id) {
        return checkoutService.getStatus(id);
    }

    public static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
