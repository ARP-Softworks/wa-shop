package uy.washop.payment.application;

import java.util.Optional;

/**
 * Abstraction over payment gateways (Mercado Pago, etc.).
 * Domain and controllers depend on this interface only.
 */
public interface PaymentProvider {

    String providerName();

    PaymentPreference createPreference(PaymentPreferenceRequest request);

    PaymentInfo getPayment(String paymentId);

    /**
     * Fallback for when the webhook notification never arrives (or is delayed):
     * looks up the most recent payment tied to this order directly, so the
     * checkout result page can reconcile the order status on its own.
     */
    Optional<PaymentInfo> findLatestPaymentByExternalReference(String externalReference);

    /** Validates a webhook notification signature. Returns false on any mismatch/parse error. */
    boolean verifyWebhookSignature(String xSignature, String xRequestId, String dataId);
}
