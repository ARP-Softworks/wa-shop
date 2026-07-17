package uy.washop.payment.application;

public record PaymentInfo(String paymentId, String status, String statusDetail, String externalReference) {
}
