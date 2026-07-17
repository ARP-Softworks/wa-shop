package uy.washop.product.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import uy.washop.category.domain.Category;
import uy.washop.category.infrastructure.CategoryRepository;
import uy.washop.config.ApplicationConfig;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductFeature;
import uy.washop.product.domain.ProductImage;
import uy.washop.product.domain.ProductType;
import uy.washop.shared.domain.CurrencyCode;

@DataJpaTest
@ActiveProfiles("test")
@EntityScan("uy.washop")
@EnableJpaRepositories("uy.washop")
@Import(ApplicationConfig.class)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductFeatureRepository productFeatureRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void persistsIphoneUsedWithImageAndFeature() {
        Category category = new Category();
        category.setName("iPhone");
        category.setSlug("iphone-" + UUID.randomUUID());
        category.setActive(true);
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setSlug("iphone-test-" + UUID.randomUUID());
        product.setName("iPhone Test");
        product.setModel("iPhone 13");
        product.setProductType(ProductType.IPHONE);
        product.setCondition(ProductCondition.USED);
        product.setStorageCapacity("128GB");
        product.setColor("Blue");
        product.setBatteryHealth(88);
        product.setPrice(new BigDecimal("24990.00"));
        product.setPreviousPrice(new BigDecimal("27990.00"));
        product.setCurrency(CurrencyCode.UYU);
        product.setStock(1);
        product.setImei("356938035643810");
        product.setPublished(true);
        product.setFeatured(false);
        product.setCategory(category);
        product = productRepository.saveAndFlush(product);

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setUrl("https://cdn.example.com/iphone.jpg");
        image.setPublicId("washop/iphone");
        image.setAltText("Frente");
        image.setPosition(0);
        image.setMainImage(true);
        productImageRepository.saveAndFlush(image);

        ProductFeature feature = new ProductFeature();
        feature.setProduct(product);
        feature.setName("Caja");
        feature.setValue("Incluye");
        productFeatureRepository.saveAndFlush(feature);

        Product loaded = productRepository.findBySlug(product.getSlug()).orElseThrow();
        List<ProductImage> images = productImageRepository.findByProductIdOrderByPositionAsc(loaded.getId());
        List<ProductFeature> features = productFeatureRepository.findByProductIdOrderByNameAsc(loaded.getId());

        assertThat(loaded.getPrice()).isEqualByComparingTo("24990.00");
        assertThat(loaded.getImei()).isEqualTo("356938035643810");
        assertThat(loaded.getBatteryHealth()).isEqualTo(88);
        assertThat(images).hasSize(1);
        assertThat(images.getFirst().isMainImage()).isTrue();
        assertThat(features).extracting(ProductFeature::getName).containsExactly("Caja");
        assertThat(productRepository.findByPublishedTrueAndProductTypeOrderByCreatedAtDesc(ProductType.IPHONE))
                .extracting(Product::getId)
                .contains(loaded.getId());
    }

    @Test
    void findsPublishedAccessoriesSeparatelyFromIphones() {
        Product accessory = new Product();
        accessory.setSlug("acc-" + UUID.randomUUID());
        accessory.setName("Cargador");
        accessory.setProductType(ProductType.ACCESSORY);
        accessory.setCondition(ProductCondition.NEW);
        accessory.setPrice(new BigDecimal("990.00"));
        accessory.setCurrency(CurrencyCode.UYU);
        accessory.setStock(5);
        accessory.setPublished(true);
        accessory.setFeatured(false);
        productRepository.saveAndFlush(accessory);

        assertThat(productRepository.findByPublishedTrueAndProductTypeOrderByCreatedAtDesc(ProductType.ACCESSORY))
                .extracting(Product::getProductType)
                .containsOnly(ProductType.ACCESSORY);
    }
}
