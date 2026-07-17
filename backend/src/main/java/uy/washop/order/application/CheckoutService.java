package uy.washop.order.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.config.AppProperties;
import uy.washop.customer.domain.Customer;
import uy.washop.customer.domain.PhoneNormalizer;
import uy.washop.customer.infrastructure.CustomerRepository;
import uy.washop.order.api.dto.OrderCreateRequest;
import uy.washop.order.api.dto.OrderCreateResponse;
import uy.washop.order.api.dto.OrderItemRequest;
import uy.washop.order.api.dto.OrderStatusResponse;
import uy.washop.order.domain.Order;
import uy.washop.order.domain.OrderItem;
import uy.washop.order.domain.OrderStatus;
import uy.washop.order.domain.OrderStatusHistory;
import uy.washop.order.infrastructure.OrderItemRepository;
import uy.washop.order.infrastructure.OrderRepository;
import uy.washop.order.infrastructure.OrderStatusHistoryRepository;
import uy.washop.payment.application.PaymentItem;
import uy.washop.payment.application.PaymentPreference;
import uy.washop.payment.application.PaymentPreferenceRequest;
import uy.washop.payment.application.PaymentProvider;
import uy.washop.product.domain.Product;
import uy.washop.product.infrastructure.ProductRepository;
import uy.washop.seo.application.SeoUrlService;
import uy.washop.shared.domain.CurrencyCode;
import uy.washop.shared.exception.BusinessConflictException;
import uy.washop.shared.exception.ResourceNotFoundException;

@Service
public class CheckoutService {

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final PaymentProvider paymentProvider;
    private final AuditService auditService;
    private final AppProperties appProperties;
    private final SeoUrlService seoUrlService;

    public CheckoutService(
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderStatusHistoryRepository historyRepository,
            PaymentProvider paymentProvider,
            AuditService auditService,
            AppProperties appProperties,
            SeoUrlService seoUrlService
    ) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.historyRepository = historyRepository;
        this.paymentProvider = paymentProvider;
        this.auditService = auditService;
        this.appProperties = appProperties;
        this.seoUrlService = seoUrlService;
    }

    @Transactional
    public OrderCreateResponse checkout(OrderCreateRequest request) {
        if (request.items().isEmpty()) {
            throw new BusinessConflictException("El carrito está vacío");
        }

        Map<UUID, Integer> requestedQuantities = new LinkedHashMap<>();
        for (OrderItemRequest item : request.items()) {
            requestedQuantities.merge(item.productId(), item.quantity(), Integer::sum);
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map.Entry<UUID, Integer> entry : requestedQuantities.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
            if (!product.isPublished()) {
                throw new BusinessConflictException("El producto " + product.getName() + " ya no está disponible");
            }
            if (product.getCurrency() != CurrencyCode.UYU) {
                throw new BusinessConflictException(
                        product.getName() + " solo se puede comprar consultando por WhatsApp"
                );
            }
            int quantity = entry.getValue();
            int affected = productRepository.reserveStock(product.getId(), quantity);
            if (affected == 0) {
                throw new BusinessConflictException("Stock insuficiente para " + product.getName());
            }

            BigDecimal lineSubtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.add(lineSubtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(quantity);
            orderItem.setSubtotal(lineSubtotal);
            orderItems.add(orderItem);
        }

        Customer customer = upsertCustomer(request);

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setSubtotal(subtotal);
        order.setTotal(subtotal);
        order.setCurrency(CurrencyCode.UYU);
        order.setShippingAddress(request.shippingAddress());
        order = orderRepository.save(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);
        }

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setFromStatus(null);
        history.setToStatus(OrderStatus.PENDING_PAYMENT);
        history.setNote("Pedido creado desde el carrito");
        historyRepository.save(history);

        PaymentPreference preference = createPreference(order, orderItems, customer);
        order.setMpPreferenceId(preference.preferenceId());
        orderRepository.save(order);

        auditService.record(
                AuditAction.CREATE,
                "Order",
                order.getId(),
                "Pedido creado por " + customer.getName() + ", total " + order.getTotal()
        );

        return new OrderCreateResponse(order.getId(), preference.checkoutUrl());
    }

    @Transactional(readOnly = true)
    public OrderStatusResponse getStatus(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        return new OrderStatusResponse(order.getId(), order.getStatus());
    }

    private Customer upsertCustomer(OrderCreateRequest request) {
        String normalizedPhone = PhoneNormalizer.normalize(request.customerPhone());
        if (!StringUtils.hasText(normalizedPhone)) {
            throw new BusinessConflictException("El teléfono es obligatorio");
        }
        Customer customer = customerRepository.findByPhoneNormalized(normalizedPhone).orElseGet(Customer::new);
        customer.setName(request.customerName());
        customer.setPhone(request.customerPhone());
        customer.setPhoneNormalized(normalizedPhone);
        if (StringUtils.hasText(request.customerEmail())) {
            customer.setEmail(request.customerEmail());
        }
        if (StringUtils.hasText(request.shippingAddress())) {
            customer.setAddress(request.shippingAddress());
        }
        return customerRepository.save(customer);
    }

    private PaymentPreference createPreference(Order order, List<OrderItem> orderItems, Customer customer) {
        List<PaymentItem> items = orderItems.stream()
                .map(oi -> new PaymentItem(oi.getProductName(), oi.getQuantity(), oi.getUnitPrice(), "UYU"))
                .toList();

        String orderId = order.getId().toString();
        PaymentPreferenceRequest preferenceRequest = new PaymentPreferenceRequest(
                orderId,
                items,
                customer.getName(),
                customer.getEmail(),
                seoUrlService.absoluteUrl("/checkout/exito?orderId=" + orderId),
                seoUrlService.absoluteUrl("/checkout/error?orderId=" + orderId),
                seoUrlService.absoluteUrl("/checkout/pendiente?orderId=" + orderId),
                SeoUrlService.trimTrailingSlash(appProperties.getPublicBaseUrl())
                        + "/api/public/mercadopago/webhook"
        );
        return paymentProvider.createPreference(preferenceRequest);
    }
}
