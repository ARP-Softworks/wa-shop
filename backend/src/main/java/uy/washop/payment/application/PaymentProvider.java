package uy.washop.payment.application;

/**
 * Abstraction over payment gateways (Mercado Pago, etc.).
 * Domain and controllers depend on this interface only.
 */
public interface PaymentProvider {

    String providerName();

    PaymentPreference createPreference(PaymentPreferenceRequest request);

    PaymentInfo getPayment(String paymentId);

    /** Validates a webhook notification signature. Returns false on any mismatch/parse error. */
    boolean verifyWebhookSignature(String xSignature, String xRequestId, String dataId);
}
