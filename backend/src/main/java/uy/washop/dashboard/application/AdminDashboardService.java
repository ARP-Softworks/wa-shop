package uy.washop.dashboard.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uy.washop.dashboard.api.dto.DashboardResponse;
import uy.washop.inquiry.api.mapper.InquiryMapper;
import uy.washop.inquiry.domain.Inquiry;
import uy.washop.inquiry.domain.InquiryStatus;
import uy.washop.inquiry.infrastructure.InquiryRepository;
import uy.washop.order.domain.OrderStatus;
import uy.washop.order.infrastructure.OrderRepository;
import uy.washop.product.api.dto.AdminProductSummaryResponse;
import uy.washop.product.api.mapper.ProductMapper;
import uy.washop.product.application.PrimaryImageUrlLoader;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.product.infrastructure.ProductRepository;
import uy.washop.product.infrastructure.ProductVariantRepository;
import uy.washop.technicalservice.api.mapper.TechnicalServiceMapper;
import uy.washop.technicalservice.infrastructure.TechnicalServiceRepository;

@Service
public class AdminDashboardService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PrimaryImageUrlLoader primaryImageUrlLoader;
    private final InquiryRepository inquiryRepository;
    private final OrderRepository orderRepository;
    private final TechnicalServiceRepository technicalServiceRepository;

    public AdminDashboardService(
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository,
            PrimaryImageUrlLoader primaryImageUrlLoader,
            InquiryRepository inquiryRepository,
            OrderRepository orderRepository,
            TechnicalServiceRepository technicalServiceRepository
    ) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.primaryImageUrlLoader = primaryImageUrlLoader;
        this.inquiryRepository = inquiryRepository;
        this.orderRepository = orderRepository;
        this.technicalServiceRepository = technicalServiceRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        List<Product> recentProducts = productRepository.findAll(
                PageRequest.of(0, 5, org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "updatedAt"))
        ).getContent();
        List<UUID> productIds = recentProducts.stream().map(Product::getId).toList();
        Map<UUID, String> images = primaryImageUrlLoader.load(productIds);
        Map<UUID, Long> variantCounts = productIds.isEmpty()
                ? Map.of()
                : productVariantRepository.findByProduct_IdIn(productIds).stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                                v -> v.getProduct().getId(),
                                java.util.stream.Collectors.counting()));
        List<AdminProductSummaryResponse> recentProductDtos = recentProducts.stream()
                .map(product -> ProductMapper.toAdminSummary(
                        product,
                        images.get(product.getId()),
                        variantCounts.getOrDefault(product.getId(), 0L).intValue()
                ))
                .toList();

        List<Inquiry> recentInquiries = inquiryRepository.findAll(
                PageRequest.of(0, 5, org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
        ).getContent();

        return new DashboardResponse(
                productRepository.countByPublishedTrue(),
                productRepository.countByPublishedTrueAndProductTypeAndCondition(
                        ProductType.IPHONE, ProductCondition.NEW),
                productRepository.countByPublishedTrueAndProductTypeAndCondition(
                        ProductType.IPHONE, ProductCondition.USED),
                inquiryRepository.countByStatus(InquiryStatus.NEW),
                orderRepository.countByStatus(OrderStatus.PENDING_PAYMENT),
                technicalServiceRepository.countByActiveTrue(),
                recentProductDtos,
                recentInquiries.stream().map(InquiryMapper::toAdminSummary).toList(),
                technicalServiceRepository.findByActiveTrueOrderByNameAsc().stream()
                        .limit(8)
                        .map(TechnicalServiceMapper::toResponse)
                        .toList()
        );
    }
}
