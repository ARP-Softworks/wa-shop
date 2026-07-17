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
import uy.washop.technicalservice.api.mapper.TechnicalServiceMapper;
import uy.washop.technicalservice.infrastructure.TechnicalServiceRepository;

@Service
public class AdminDashboardService {

    private final ProductRepository productRepository;
    private final PrimaryImageUrlLoader primaryImageUrlLoader;
    private final InquiryRepository inquiryRepository;
    private final OrderRepository orderRepository;
    private final TechnicalServiceRepository technicalServiceRepository;

    public AdminDashboardService(
            ProductRepository productRepository,
            PrimaryImageUrlLoader primaryImageUrlLoader,
            InquiryRepository inquiryRepository,
            OrderRepository orderRepository,
            TechnicalServiceRepository technicalServiceRepository
    ) {
        this.productRepository = productRepository;
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
        Map<UUID, String> images = primaryImageUrlLoader.load(recentProducts.stream().map(Product::getId).toList());
        List<AdminProductSummaryResponse> recentProductDtos = recentProducts.stream()
                .map(product -> ProductMapper.toAdminSummary(product, images.get(product.getId())))
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
