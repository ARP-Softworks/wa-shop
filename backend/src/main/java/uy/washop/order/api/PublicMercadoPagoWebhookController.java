package uy.washop.order.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.order.api.dto.MercadoPagoWebhookPayload;
import uy.washop.order.application.OrderWebhookService;
import uy.washop.payment.application.PaymentProvider;

@RestController
@RequestMapping("/api/public/mercadopago")
public class PublicMercadoPagoWebhookController {

    private final OrderWebhookService webhookService;
    private final PaymentProvider paymentProvider;

    public PublicMercadoPagoWebhookController(OrderWebhookService webhookService, PaymentProvider paymentProvider) {
        this.webhookService = webhookService;
        this.paymentProvider = paymentProvider;
    }

    /**
     * Server-to-server notification from Mercado Pago — exempted from CSRF in SecurityConfig
     * (see the comment there) and instead authenticated via x-signature HMAC verification.
     * Always returns 200 unless the signature is invalid, to avoid infinite MP retries on
     * transient/unmatched notifications.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestParam(value = "data.id", required = false) String dataIdParam,
            @RequestBody(required = false) MercadoPagoWebhookPayload payload
    ) {
        String dataId = StringUtils.hasText(dataIdParam)
                ? dataIdParam
                : (payload != null && payload.data() != null ? payload.data().id() : null);

        if (!StringUtils.hasText(dataId)) {
            return ResponseEntity.ok().build();
        }
        if (!paymentProvider.verifyWebhookSignature(xSignature, xRequestId, dataId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        webhookService.processNotification(dataId);
        return ResponseEntity.ok().build();
    }
}
