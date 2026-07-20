package uy.washop.order.application;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uy.washop.order.domain.OrderItem;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductVariant;
import uy.washop.product.infrastructure.ProductRepository;
import uy.washop.product.infrastructure.ProductVariantRepository;

@Service
public class VariantStockService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    public VariantStockService(
            ProductVariantRepository productVariantRepository,
            ProductRepository productRepository
    ) {
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public int reserve(UUID variantId, int quantity) {
        int affected = productVariantRepository.reserveStock(variantId, quantity);
        if (affected > 0) {
            productVariantRepository.findById(variantId).ifPresent(variant ->
                    syncParentStock(variant.getProduct().getId()));
        }
        return affected;
    }

    @Transactional
    public void restore(OrderItem item) {
        if (item.getVariantId() != null) {
            productVariantRepository.restoreStock(item.getVariantId(), item.getQuantity());
            if (item.getProductId() != null) {
                syncParentStock(item.getProductId());
            } else {
                productVariantRepository.findById(item.getVariantId()).ifPresent(variant ->
                        syncParentStock(variant.getProduct().getId()));
            }
            return;
        }
        if (item.getProductId() != null) {
            productRepository.restoreStock(item.getProductId(), item.getQuantity());
        }
    }

    private void syncParentStock(UUID productId) {
        List<ProductVariant> variants = productVariantRepository.findByProduct_IdOrderByPriceAsc(productId);
        int total = variants.stream().mapToInt(ProductVariant::getStock).sum();
        productRepository.findById(productId).ifPresent(product -> {
            product.setStock(total);
            productRepository.save(product);
        });
    }
}
