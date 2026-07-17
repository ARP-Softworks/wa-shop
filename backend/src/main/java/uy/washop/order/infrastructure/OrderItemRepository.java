package uy.washop.order.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uy.washop.order.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderId(UUID orderId);
}
