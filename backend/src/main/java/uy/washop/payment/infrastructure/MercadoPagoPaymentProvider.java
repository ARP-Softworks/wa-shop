package uy.washop.payment.infrastructure;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uy.washop.config.AppProperties;
import uy.washop.payment.application.PaymentInfo;
import uy.washop.payment.application.PaymentItem;
import uy.washop.payment.application.PaymentPreference;
import uy.washop.payment.application.PaymentPreferenceRequest;
import uy.washop.payment.application.PaymentProvider;
import uy.washop.shared.exception.BusinessConflictException;

@Service
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "mercadopago")
public class MercadoPagoPaymentProvider implements PaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoPaymentProvider.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String webhookSecret;
    private final boolean configured;

    public MercadoPagoPaymentProvider(AppProperties appProperties) {
        AppProperties.Payment.MercadoPago props = appProperties.getPayment().getMercadoPago();
        // Credentials are validated lazily (on first real call) rather than at boot, so the app
        // still starts in dev/test environments where Mercado Pago hasn't been configured yet —
        // same reasoning as WhatsApp not being configured: the feature is just unusable until set.
        this.configured = StringUtils.hasText(props.getAccessToken()) && StringUtils.hasText(props.getPublicKey());
        if (this.configured) {
            MercadoPagoConfig.setAccessToken(props.getAccessToken());
        } else {
            log.warn("Mercado Pago is not configured (MERCADOPAGO_ACCESS_TOKEN/PUBLIC_KEY missing) — checkout will fail until it is set");
        }
        this.webhookSecret = props.getWebhookSecret();
    }

    private void requireConfigured() {
        if (!configured) {
            throw new BusinessConflictException("Mercado Pago no está configurado en el servidor");
        }
    }

    @Override
    public String providerName() {
        return "mercadopago";
    }

    @Override
    public PaymentPreference createPreference(PaymentPreferenceRequest request) {
        requireConfigured();
        try {
            List<PreferenceItemRequest> items = request.items().stream()
                    .map(MercadoPagoPaymentProvider::toItemRequest)
                    .toList();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(request.successUrl())
                    .pending(request.pendingUrl())
                    .failure(request.failureUrl())
                    .build();

            PreferenceRequest.PreferenceRequestBuilder builder = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .externalReference(request.externalReference())
                    .notificationUrl(request.notificationUrl());

            // Mercado Pago rejects auto_return unless back_url.success is a valid HTTPS URL —
            // it errors out on http://localhost, so only enable it once the site is on HTTPS
            // (production). In local dev the buyer just clicks "Volver al sitio" manually.
            if (request.successUrl() != null && request.successUrl().startsWith("https://")) {
                builder.autoReturn("approved");
            }

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(builder.build());
            String checkoutUrl = preference.getInitPoint() != null
                    ? preference.getInitPoint()
                    : preference.getSandboxInitPoint();
            return new PaymentPreference(preference.getId(), checkoutUrl);
        } catch (MPApiException ex) {
            log.error("Mercado Pago preference creation failed: {}", ex.getApiResponse().getContent(), ex);
            throw new BusinessConflictException("No se pudo iniciar el pago con Mercado Pago");
        } catch (MPException ex) {
            log.error("Mercado Pago preference creation failed", ex);
            throw new BusinessConflictException("No se pudo iniciar el pago con Mercado Pago");
        }
    }

    @Override
    public PaymentInfo getPayment(String paymentId) {
        requireConfigured();
        try {
            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment payment = client.get(Long.parseLong(paymentId));
            return new PaymentInfo(
                    String.valueOf(payment.getId()),
                    payment.getStatus(),
                    payment.getStatusDetail(),
                    payment.getExternalReference()
            );
        } catch (MPApiException ex) {
            log.error("Mercado Pago payment lookup failed: {}", ex.getApiResponse().getContent(), ex);
            throw new BusinessConflictException("No se pudo consultar el pago en Mercado Pago");
        } catch (MPException ex) {
            log.error("Mercado Pago payment lookup failed", ex);
            throw new BusinessConflictException("No se pudo consultar el pago en Mercado Pago");
        }
    }

    @Override
    public boolean verifyWebhookSignature(String xSignature, String xRequestId, String dataId) {
        if (!StringUtils.hasText(webhookSecret)
                || !StringUtils.hasText(xSignature)
                || !StringUtils.hasText(xRequestId)
                || !StringUtils.hasText(dataId)) {
            return false;
        }
        Map<String, String> parts = parseSignatureHeader(xSignature);
        String ts = parts.get("ts");
        String v1 = parts.get("v1");
        if (ts == null || v1 == null) {
            return false;
        }
        String manifest = "id:" + dataId.toLowerCase() + ";request-id:" + xRequestId + ";ts:" + ts + ";";
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] computed = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
            String computedHex = toHex(computed);
            return MessageDigest.isEqual(
                    computedHex.getBytes(StandardCharsets.UTF_8),
                    v1.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception ex) {
            log.error("Mercado Pago webhook signature verification failed", ex);
            return false;
        }
    }

    private static Map<String, String> parseSignatureHeader(String xSignature) {
        Map<String, String> parts = new HashMap<>();
        for (String segment : xSignature.split(",")) {
            String[] kv = segment.split("=", 2);
            if (kv.length == 2) {
                parts.put(kv[0].trim(), kv[1].trim());
            }
        }
        return parts;
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static PreferenceItemRequest toItemRequest(PaymentItem item) {
        return PreferenceItemRequest.builder()
                .title(item.title())
                .quantity(item.quantity())
                .unitPrice(item.unitPrice())
                .currencyId(item.currencyId())
                .build();
    }
}
