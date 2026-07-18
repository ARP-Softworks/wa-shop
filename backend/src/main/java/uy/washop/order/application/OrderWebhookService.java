package uy.washop.order.application;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.order.domain.Order;
import uy.washop.order.domain.OrderItem;
import uy.washop.order.domain.OrderStatus;
import uy.washop.order.domain.OrderStatusHistory;
import uy.washop.order.infrastructure.OrderItemRepository;
import uy.washop.order.infrastructure.OrderRepository;
import uy.washop.order.infrastructure.OrderStatusHistoryRepository;
import uy.washop.payment.application.PaymentInfo;
import uy.washop.payment.application.PaymentProvider;
import uy.washop.product.infrastructure.ProductRepository;

@Service
public class OrderWebhookService {

    private static final Logger log = LoggerFactory.getLogger(OrderWebhookService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final ProductRepository productRepository;
    private final PaymentProvider paymentProvider;
    private final AuditService auditService;

    public OrderWebhookService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderStatusHistoryRepository historyRepository,
            ProductRepository productRepository,
            PaymentProvider paymentProvider,
            AuditService auditService
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.historyRepository = historyRepository;
        this.productRepository = productRepository;
        this.paymentProvider = paymentProvider;
        this.auditService = auditService;
    }

    @Transactional
    public void processNotification(String paymentId) {
        PaymentInfo info;
        try {
            info = paymentProvider.getPayment(paymentId);
        } catch (Exception ex) {
            log.warn("Could not fetch Mercado Pago payment {}: {}", paymentId, ex.getMessage());
            return;
        }
        if (info.externalReference() == null) {
            return;
        }
        UUID orderId;
        try {
            orderId = UUID.fromString(info.externalReference());
        } catch (IllegalArgumentException ex) {
            log.warn("Mercado Pago payment {} has unrecognized external_reference {}", paymentId, info.externalReference());
            return;
        }
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("Mercado Pago payment {} references unknown order {}", paymentId, orderId);
            return;
        }

        boolean alreadyProcessed = paymentId.equals(order.getMpPaymentId())
                && info.status() != null
                && info.status().equals(order.getMpPaymentStatus());
        if (alreadyProcessed) {
            return;
        }

        OrderStatus previous = order.getStatus();
        order.setMpPaymentId(paymentId);
        order.setMpPaymentStatus(info.status());

        if (previous != OrderStatus.PENDING_PAYMENT) {
            // Order already reached a terminal/confirmed state — never silently flip it back
            // (e.g. late webhook after expiry restored stock, or after admin already shipped it).
            orderRepository.save(order);
            auditService.record(
                    AuditAction.OTHER,
                    "Order",
                    order.getId(),
                    "Notificación MP tardía (estado actual " + previous + "): " + info.status()
                            + " — requiere revisión manual si corresponde"
            );
            return;
        }

        if ("approved".equals(info.status()) && !paymentMatchesOrder(order, info)) {
            orderRepository.save(order);
            auditService.record(
                    AuditAction.OTHER,
                    "Order",
                    order.getId(),
                    "Pago MP approved con monto/moneda inconsistente (pagado="
                            + info.transactionAmount() + " " + info.currencyId()
                            + ", pedido=" + order.getTotal() + " " + order.getCurrency()
                            + ") — no se marcó PAID; revisión manual"
            );
            log.warn(
                    "Rejected Mercado Pago approval for order {} due to amount/currency mismatch (paid={} {}, expected={} {})",
                    order.getId(),
                    info.transactionAmount(),
                    info.currencyId(),
                    order.getTotal(),
                    order.getCurrency()
            );
            return;
        }

        OrderStatus newStatus = mapMercadoPagoStatus(info.status());
        if (newStatus == previous) {
            orderRepository.save(order);
            return;
        }

        if (newStatus == OrderStatus.REJECTED || newStatus == OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setFromStatus(previous);
        history.setToStatus(newStatus);
        history.setNote("Actualizado por webhook de Mercado Pago (" + info.status() + ")");
        historyRepository.save(history);

        auditService.record(
                AuditAction.UPDATE,
                "Order",
                order.getId(),
                "Pedido actualizado a " + newStatus + " por Mercado Pago"
        );
    }

    static boolean paymentMatchesOrder(Order order, PaymentInfo info) {
        if (info.transactionAmount() == null || !StringUtils.hasText(info.currencyId())) {
            return false;
        }
        if (order.getTotal() == null || order.getCurrency() == null) {
            return false;
        }
        if (info.transactionAmount().compareTo(order.getTotal()) != 0) {
            return false;
        }
        return order.getCurrency().name().equalsIgnoreCase(info.currencyId().trim());
    }

    private void restoreStock(Order order) {
        for (OrderItem item : orderItemRepository.findByOrderId(order.getId())) {
            if (item.getProductId() != null) {
                productRepository.restoreStock(item.getProductId(), item.getQuantity());
            }
        }
    }

    private static OrderStatus mapMercadoPagoStatus(String mpStatus) {
        if (mpStatus == null) {
            return OrderStatus.PENDING_PAYMENT;
        }
        return switch (mpStatus) {
            case "approved" -> OrderStatus.PAID;
            case "rejected" -> OrderStatus.REJECTED;
            case "cancelled" -> OrderStatus.CANCELLED;
            default -> OrderStatus.PENDING_PAYMENT; // pending, in_process, authorized, etc.
        };
    }
}
