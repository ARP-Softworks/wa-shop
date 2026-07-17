package uy.washop.order.domain;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REJECTED,
    EXPIRED
}
