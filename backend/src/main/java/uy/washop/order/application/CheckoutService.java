package uy.washop.order.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import uy.washop.promotion.application.CategoryCartLine;
import uy.washop.promotion.application.PromotionEngine;
import uy.washop.promotion.domain.Promotion;
import uy.washop.promotion.infrastructure.PromotionRepository;
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
    private final PromotionRepository promotionRepository;

    public CheckoutService(
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderStatusHistoryRepository historyRepository,
            PaymentProvider paymentProvider,
            AuditService auditService,
            AppProperties appProperties,
            SeoUrlService seoUrlService,
            PromotionRepository promotionRepository
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
        this.promotionRepository = promotionRepository;
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

            BigDecimal lineSubtotal = calculateLineSubtotal(product, quantity);
            subtotal = subtotal.add(lineSubtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(quantity);
            orderItem.setSubtotal(lineSubtotal);
            orderItems.add(orderItem);
        }

        Map<UUID, BigDecimal> promotionDiscounts = calculatePromotionDiscounts(orderItems);
        BigDecimal totalDiscount = PromotionEngine.totalDiscount(promotionDiscounts);

        Customer customer = upsertCustomer(request);

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setSubtotal(subtotal);
        order.setPromotionDiscount(totalDiscount);
        order.setTotal(subtotal.subtract(totalDiscount));
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

        PaymentPreference preference = createPreference(order, orderItems, customer, promotionDiscounts);
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

    /** Applies "buy X pay Y" promos (2x1, 3x2, ...): full-price for the non-bundled remainder. */
    static BigDecimal calculateLineSubtotal(Product product, int quantity) {
        BigDecimal unitPrice = product.getPrice();
        if (!product.hasActivePromotion()) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        int buy = product.getPromoBuyQuantity();
        int pay = product.getPromoPayQuantity();
        int bundles = quantity / buy;
        int remainder = quantity % buy;
        int payableUnits = bundles * pay + remainder;
        return unitPrice.multiply(BigDecimal.valueOf(payableUnits));
    }

    /** Cross-category "buy N, get M at X% off" promotions, layered on top of any per-product promo already baked into each line's subtotal. */
    private Map<UUID, BigDecimal> calculatePromotionDiscounts(List<OrderItem> orderItems) {
        List<Promotion> activePromotions = promotionRepository.findByActiveTrueOrderByNameAsc();
        if (activePromotions.isEmpty()) {
            return Map.of();
        }
        List<CategoryCartLine> lines = orderItems.stream()
                .filter(oi -> oi.getProduct().getCategoryId() != null)
                .map(oi -> new CategoryCartLine(
                        oi.getProduct().getId(),
                        oi.getProduct().getCategoryId(),
                        oi.getQuantity(),
                        oi.getSubtotal().divide(BigDecimal.valueOf(oi.getQuantity()), 4, RoundingMode.HALF_UP)
                ))
                .toList();
        return PromotionEngine.calculateDiscounts(lines, activePromotions);
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

    private PaymentPreference createPreference(
            Order order, List<OrderItem> orderItems, Customer customer, Map<UUID, BigDecimal> promotionDiscounts
    ) {
        // Quantity is always sent as 1 with the (already promo-discounted) line subtotal as the
        // unit price — this guarantees the amount Mercado Pago charges exactly matches
        // order.getTotal(), with no rounding drift from dividing a promo price across units.
        // Cross-category promotion discounts are subtracted per line rather than sent as a
        // separate negative item — Mercado Pago rejects zero/negative unit prices, so a line
        // fully covered by a promotion (100% off) is dropped instead of sent as $0.
        List<PaymentItem> items = orderItems.stream()
                .map(oi -> {
                    BigDecimal discount = promotionDiscounts.getOrDefault(oi.getProduct().getId(), BigDecimal.ZERO);
                    BigDecimal adjustedPrice = oi.getSubtotal().subtract(discount).max(BigDecimal.ZERO);
                    return new PaymentItem(oi.getProductName() + " × " + oi.getQuantity(), 1, adjustedPrice, "UYU");
                })
                .filter(item -> item.unitPrice().compareTo(BigDecimal.ZERO) > 0)
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
