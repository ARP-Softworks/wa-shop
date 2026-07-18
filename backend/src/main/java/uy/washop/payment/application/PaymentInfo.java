package uy.washop.payment.application;

import java.math.BigDecimal;

public record PaymentInfo(
        String paymentId,
        String status,
        String statusDetail,
        String externalReference,
        BigDecimal transactionAmount,
        String currencyId
) {
}
