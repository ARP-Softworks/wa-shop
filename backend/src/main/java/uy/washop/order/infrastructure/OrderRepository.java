package uy.washop.order.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uy.washop.order.domain.Order;
import uy.washop.order.domain.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, Instant threshold);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByCustomer_Id(UUID customerId, Pageable pageable);

    long countByStatus(OrderStatus status);
}
