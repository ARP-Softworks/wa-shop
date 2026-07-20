package uy.washop.order.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.config.AppProperties;
import uy.washop.order.domain.Order;
import uy.washop.order.domain.OrderItem;
import uy.washop.order.domain.OrderStatus;
import uy.washop.order.domain.OrderStatusHistory;
import uy.washop.order.infrastructure.OrderItemRepository;
import uy.washop.order.infrastructure.OrderRepository;
import uy.washop.order.infrastructure.OrderStatusHistoryRepository;
/** Releases stock reserved by carts abandoned mid-checkout (never paid). */
@Service
public class OrderExpiryService {

    private static final Logger log = LoggerFactory.getLogger(OrderExpiryService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final VariantStockService variantStockService;
    private final AuditService auditService;
    private final AppProperties appProperties;

    public OrderExpiryService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderStatusHistoryRepository historyRepository,
            VariantStockService variantStockService,
            AuditService auditService,
            AppProperties appProperties
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.historyRepository = historyRepository;
        this.variantStockService = variantStockService;
        this.auditService = auditService;
        this.appProperties = appProperties;
    }

    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT1M")
    @Transactional
    public void expireAbandonedOrders() {
        Instant threshold = Instant.now().minus(
                appProperties.getOrder().getPendingExpiryMinutes(), ChronoUnit.MINUTES
        );
        List<Order> abandoned = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING_PAYMENT, threshold);
        for (Order order : abandoned) {
            for (OrderItem item : orderItemRepository.findByOrderId(order.getId())) {
                variantStockService.restore(item);
            }
            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);

            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrder(order);
            history.setFromStatus(OrderStatus.PENDING_PAYMENT);
            history.setToStatus(OrderStatus.EXPIRED);
            history.setNote("Pedido expirado automáticamente sin pago confirmado");
            historyRepository.save(history);

            auditService.record(AuditAction.OTHER, "Order", order.getId(), "Pedido expirado, stock restaurado");
        }
        if (!abandoned.isEmpty()) {
            log.info("Expired {} abandoned orders", abandoned.size());
        }
    }
}
