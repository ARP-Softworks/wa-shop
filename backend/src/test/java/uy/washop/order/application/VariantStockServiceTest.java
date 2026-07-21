package uy.washop.order.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.product.domain.ProductVariant;
import uy.washop.product.infrastructure.ProductRepository;
import uy.washop.product.infrastructure.ProductVariantRepository;
import uy.washop.shared.domain.CurrencyCode;

/**
 * Regression test for a stale-entity bug: CheckoutService loads the ProductVariant via
 * findById() BEFORE reserving stock (to validate it, read its price, etc.), which manages
 * that entity in the transaction's persistence context. reserve() then runs a bulk
 * @Modifying UPDATE that bypasses that context — so a naive "reload variants and sum in
 * Java" resync afterward would hand back the pre-decrement cached instance instead of the
 * real post-update value, and silently persist the wrong Product.stock.
 */
@SpringBootTest
@ActiveProfiles("test")
class VariantStockServiceTest {

    @Autowired
    private VariantStockService variantStockService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Test
    @Transactional
    void syncsParentStockCorrectlyEvenWhenVariantWasAlreadyLoadedInTheSameTransaction() {
        Product product = new Product();
        product.setName("Stock Sync Test");
        product.setSlug("stock-sync-test-" + UUID.randomUUID());
        product.setProductType(ProductType.ACCESSORY);
        product.setCondition(ProductCondition.NEW);
        product.setPrice(new BigDecimal("1000.00"));
        product.setCurrency(CurrencyCode.UYU);
        product.setStock(5);
        product.setPublished(true);
        product = productRepository.save(product);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setCondition(ProductCondition.NEW);
        variant.setPrice(new BigDecimal("1000.00"));
        variant.setCurrency(CurrencyCode.UYU);
        variant.setStock(5);
        variant.setPublished(true);
        variant = productVariantRepository.save(variant);

        // Mirrors CheckoutService: load the variant into the persistence context BEFORE reserving.
        ProductVariant preloaded = productVariantRepository.findById(variant.getId()).orElseThrow();
        assertThat(preloaded.getStock()).isEqualTo(5);

        int affected = variantStockService.reserve(variant.getId(), 1);

        assertThat(affected).isEqualTo(1);
        // Read the true post-update total via a scalar aggregate (not an entity fetch) — an entity
        // re-fetch of the *variant itself* is a separate, pre-existing Hibernate identity-map quirk
        // that stays stale within this same transaction regardless of this fix, but is harmless
        // because CheckoutService never reads the variant's stock again after reserving it.
        assertThat(productVariantRepository.sumStockByProductId(product.getId())).isEqualTo(4);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock())
                .as("Product.stock must reflect the real post-reservation total, not a stale cached sum")
                .isEqualTo(4);
    }
}
