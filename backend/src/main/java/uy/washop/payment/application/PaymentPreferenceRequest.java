package uy.washop.payment.application;

import java.util.List;

public record PaymentPreferenceRequest(
        String externalReference,
        List<PaymentItem> items,
        String payerName,
        String payerEmail,
        String successUrl,
        String failureUrl,
        String pendingUrl,
        String notificationUrl
) {
}
