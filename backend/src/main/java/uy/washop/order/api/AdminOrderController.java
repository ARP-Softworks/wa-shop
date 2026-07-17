package uy.washop.order.api;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.order.api.dto.AdminOrderDetailResponse;
import uy.washop.order.api.dto.AdminOrderSummaryResponse;
import uy.washop.order.api.dto.OrderStatusUpdateRequest;
import uy.washop.order.application.AdminOrderService;
import uy.washop.order.domain.OrderStatus;
import uy.washop.shared.api.PageResponse;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public PageResponse<AdminOrderSummaryResponse> search(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminOrderService.search(status, customerId, page, size);
    }

    @GetMapping("/{id}")
    public AdminOrderDetailResponse get(@PathVariable UUID id) {
        return adminOrderService.getById(id);
    }

    @PatchMapping("/{id}/status")
    public AdminOrderDetailResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        return adminOrderService.updateStatus(id, request);
    }
}
