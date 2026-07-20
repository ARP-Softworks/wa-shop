package uy.washop.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.order.domain.Order;
import uy.washop.order.domain.OrderStatus;
import uy.washop.order.infrastructure.OrderItemRepository;
import uy.washop.order.infrastructure.OrderRepository;
import uy.washop.order.infrastructure.OrderStatusHistoryRepository;
import uy.washop.notification.application.OrderEmailService;
import uy.washop.payment.application.PaymentInfo;
import uy.washop.payment.application.PaymentProvider;
import uy.washop.product.infrastructure.ProductRepository;
import uy.washop.shared.domain.CurrencyCode;

@ExtendWith(MockitoExtension.class)
class OrderWebhookServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private OrderStatusHistoryRepository historyRepository;
    @Mock private ProductRepository productRepository;
    @Mock private PaymentProvider paymentProvider;
    @Mock private AuditService auditService;
    @Mock private OrderEmailService orderEmailService;

    private OrderWebhookService service;

    @BeforeEach
    void setUp() {
        service = new OrderWebhookService(
                orderRepository,
                orderItemRepository,
                historyRepository,
                productRepository,
                paymentProvider,
                auditService,
                orderEmailService
        );
    }

    @Test
    void marksPaidWhenApprovedAmountAndCurrencyMatch() {
        UUID orderId = UUID.randomUUID();
        Order order = pendingOrder(orderId, new BigDecimal("1500.00"));
        when(paymentProvider.getPayment("pay-1")).thenReturn(approved("pay-1", orderId, new BigDecimal("1500.00"), "UYU"));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        service.processNotification("pay-1");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(historyRepository).save(any());
        verify(auditService).record(eq(AuditAction.UPDATE), eq("Order"), eq(orderId), anyString());
    }

    @Test
    void doesNotMarkPaidWhenAmountMismatches() {
        UUID orderId = UUID.randomUUID();
        Order order = pendingOrder(orderId, new BigDecimal("1500.00"));
        when(paymentProvider.getPayment("pay-1")).thenReturn(approved("pay-1", orderId, new BigDecimal("1.00"), "UYU"));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        service.processNotification("pay-1");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.getMpPaymentStatus()).isEqualTo("approved");
        verify(historyRepository, never()).save(any());
        ArgumentCaptor<String> note = ArgumentCaptor.forClass(String.class);
        verify(auditService).record(eq(AuditAction.OTHER), eq("Order"), eq(orderId), note.capture());
        assertThat(note.getValue()).contains("inconsistente");
    }

    @Test
    void doesNotMarkPaidWhenCurrencyMismatches() {
        UUID orderId = UUID.randomUUID();
        Order order = pendingOrder(orderId, new BigDecimal("1500.00"));
        when(paymentProvider.getPayment("pay-1")).thenReturn(approved("pay-1", orderId, new BigDecimal("1500.00"), "ARS"));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        service.processNotification("pay-1");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        verify(historyRepository, never()).save(any());
    }

    @Test
    void paymentMatchesOrderRequiresExactAmountAndCurrency() {
        Order order = pendingOrder(UUID.randomUUID(), new BigDecimal("99.50"));
        assertThat(OrderWebhookService.paymentMatchesOrder(
                order,
                approved("p", order.getId(), new BigDecimal("99.50"), "UYU")
        )).isTrue();
        assertThat(OrderWebhookService.paymentMatchesOrder(
                order,
                approved("p", order.getId(), new BigDecimal("99.5"), "uyu")
        )).isTrue();
        assertThat(OrderWebhookService.paymentMatchesOrder(
                order,
                approved("p", order.getId(), new BigDecimal("99.51"), "UYU")
        )).isFalse();
        assertThat(OrderWebhookService.paymentMatchesOrder(
                order,
                new PaymentInfo("p", "approved", null, order.getId().toString(), null, "UYU")
        )).isFalse();
    }

    private static Order pendingOrder(UUID id, BigDecimal total) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setSubtotal(total);
        order.setTotal(total);
        order.setCurrency(CurrencyCode.UYU);
        return order;
    }

    private static PaymentInfo approved(String paymentId, UUID orderId, BigDecimal amount, String currency) {
        return new PaymentInfo(paymentId, "approved", "accredited", orderId.toString(), amount, currency);
    }
}
