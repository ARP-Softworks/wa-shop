package uy.washop.discount.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.discount.api.dto.DiscountCodeValidateRequest;
import uy.washop.discount.api.dto.DiscountCodeValidateResponse;
import uy.washop.discount.application.DiscountCodeApplicationService;
import uy.washop.discount.application.DiscountValidateRateLimitService;
import uy.washop.order.api.PublicOrderController;

@RestController
@RequestMapping("/api/public/discount-codes")
public class PublicDiscountCodeController {

    private final DiscountCodeApplicationService discountCodeApplicationService;
    private final DiscountValidateRateLimitService rateLimitService;

    public PublicDiscountCodeController(
            DiscountCodeApplicationService discountCodeApplicationService,
            DiscountValidateRateLimitService rateLimitService
    ) {
        this.discountCodeApplicationService = discountCodeApplicationService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/validate")
    public DiscountCodeValidateResponse validate(
            @Valid @RequestBody DiscountCodeValidateRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = PublicOrderController.resolveClientIp(httpRequest);
        rateLimitService.assertAllowed(clientIp);
        rateLimitService.recordAttempt(clientIp);

        var applied = discountCodeApplicationService.preview(request.code(), request.eligibleSubtotal());
        return new DiscountCodeValidateResponse(
                applied.code().getCode(),
                applied.code().getDiscountType(),
                applied.code().getDiscountValue(),
                applied.amount(),
                request.eligibleSubtotal()
        );
    }
}
