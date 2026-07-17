package uy.washop.order.api.dto;

public record MercadoPagoWebhookPayload(String type, String action, Data data) {
    public record Data(String id) {
    }
}
