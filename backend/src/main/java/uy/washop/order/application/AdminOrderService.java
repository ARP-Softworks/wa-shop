package uy.washop.order.application;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.order.api.dto.AdminOrderDetailResponse;
import uy.washop.order.api.dto.AdminOrderSummaryResponse;
import uy.washop.order.api.dto.OrderStatusUpdateRequest;
import uy.washop.order.api.mapper.OrderMapper;
import uy.washop.order.domain.Order;
import uy.washop.order.domain.OrderItem;
import uy.washop.order.domain.OrderStatus;
import uy.washop.order.domain.OrderStatusHistory;
import uy.washop.order.infrastructure.OrderItemRepository;
import uy.washop.order.infrastructure.OrderRepository;
import uy.washop.order.infrastructure.OrderStatusHistoryRepository;
import uy.washop.product.infrastructure.ProductRepository;
import uy.washop.shared.api.PageResponse;
import uy.washop.shared.exception.ResourceNotFoundException;

@Service
public class AdminOrderService {

    private static final Set<OrderStatus> STOCK_RESERVED_STATUSES = Set.of(
            OrderStatus.PENDING_PAYMENT, OrderStatus.PAID, OrderStatus.CONFIRMED, OrderStatus.SHIPPED
    );

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public AdminOrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderStatusHistoryRepository historyRepository,
            ProductRepository productRepository,
            AuditService auditService
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.historyRepository = historyRepository;
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminOrderSummaryResponse> search(OrderStatus status, UUID customerId, int page, int size) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 48),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<Order> result;
        if (customerId != null) {
            result = orderRepository.findByCustomer_Id(customerId, pageable);
        } else if (status != null) {
            result = orderRepository.findByStatus(status, pageable);
        } else {
            result = orderRepository.findAll(pageable);
        }
        return new PageResponse<>(
                result.getContent().stream()
                        .map(order -> OrderMapper.toAdminSummary(order, orderItemRepository.findByOrderId(order.getId()).size()))
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public AdminOrderDetailResponse getById(UUID id) {
        Order order = require(id);
        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        List<OrderStatusHistory> history = historyRepository.findByOrderIdOrderByCreatedAtDesc(id);
        return OrderMapper.toAdminDetail(order, items, history);
    }

    @Transactional
    public AdminOrderDetailResponse updateStatus(UUID id, OrderStatusUpdateRequest request) {
        Order order = require(id);
        OrderStatus previous = order.getStatus();

        if (request.status() != previous) {
            boolean releasingStock = STOCK_RESERVED_STATUSES.contains(previous)
                    && (request.status() == OrderStatus.CANCELLED || request.status() == OrderStatus.REJECTED);
            if (releasingStock) {
                for (OrderItem item : orderItemRepository.findByOrderId(id)) {
                    if (item.getProductId() != null) {
                        productRepository.restoreStock(item.getProductId(), item.getQuantity());
                    }
                }
            }

            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrder(order);
            history.setFromStatus(previous);
            history.setToStatus(request.status());
            history.setChangedBy(auditService.currentUserId());
            history.setNote(StringUtils.hasText(request.note()) ? request.note().trim() : null);
            historyRepository.save(history);
            order.setStatus(request.status());
        }
        orderRepository.save(order);
        auditService.record(AuditAction.UPDATE, "Order", id, "Pedido actualizado a " + order.getStatus());

        return getById(id);
    }

    private Order require(UUID id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
    }
}
