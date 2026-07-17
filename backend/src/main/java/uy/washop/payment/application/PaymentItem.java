package uy.washop.payment.application;

import java.math.BigDecimal;

public record PaymentItem(String title, int quantity, BigDecimal unitPrice, String currencyId) {
}
