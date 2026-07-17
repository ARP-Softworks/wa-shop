package uy.washop.order.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uy.washop.order.domain.OrderStatusHistory;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {

    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtDesc(UUID orderId);
}
