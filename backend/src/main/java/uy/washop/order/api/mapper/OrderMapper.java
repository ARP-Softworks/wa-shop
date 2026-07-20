package uy.washop.order.api.mapper;

import java.util.List;
import uy.washop.order.api.dto.AdminOrderDetailResponse;
import uy.washop.order.api.dto.AdminOrderSummaryResponse;
import uy.washop.order.api.dto.OrderItemResponse;
import uy.washop.order.api.dto.OrderStatusHistoryResponse;
import uy.washop.order.domain.Order;
import uy.washop.order.domain.OrderItem;
import uy.washop.order.domain.OrderStatusHistory;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static AdminOrderSummaryResponse toAdminSummary(Order order, int itemCount) {
        return new AdminOrderSummaryResponse(
                order.getId(),
                order.getCustomerId(),
                order.getCustomer() != null ? order.getCustomer().getName() : null,
                order.getCustomer() != null ? order.getCustomer().getPhone() : null,
                order.getStatus(),
                order.getTotal(),
                order.getCurrency(),
                itemCount,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public static AdminOrderDetailResponse toAdminDetail(
            Order order,
            List<OrderItem> items,
            List<OrderStatusHistory> history
    ) {
        return new AdminOrderDetailResponse(
                order.getId(),
                order.getCustomerId(),
                order.getCustomer() != null ? order.getCustomer().getName() : null,
                order.getCustomer() != null ? order.getCustomer().getPhone() : null,
                order.getCustomer() != null ? order.getCustomer().getEmail() : null,
                order.getStatus(),
                order.getSubtotal(),
                order.getPromotionDiscount(),
                order.getDiscountCode(),
                order.getCouponDiscount(),
                order.getTotal(),
                order.getCurrency(),
                order.getShippingAddress(),
                order.getMpPreferenceId(),
                order.getMpPaymentId(),
                order.getMpPaymentStatus(),
                order.getNotes(),
                items.stream().map(OrderMapper::toItemResponse).toList(),
                history.stream().map(OrderMapper::toHistoryResponse).toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public static OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getVariantId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }

    public static OrderStatusHistoryResponse toHistoryResponse(OrderStatusHistory entry) {
        return new OrderStatusHistoryResponse(
                entry.getId(),
                entry.getFromStatus(),
                entry.getToStatus(),
                entry.getChangedBy(),
                entry.getNote(),
                entry.getCreatedAt()
        );
    }
}
