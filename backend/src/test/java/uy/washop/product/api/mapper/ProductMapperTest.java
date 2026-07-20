package uy.washop.product.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uy.washop.product.api.dto.ProductAdminResponse;
import uy.washop.product.api.dto.ProductPublicResponse;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.product.domain.ProductVariant;
import uy.washop.shared.domain.CurrencyCode;

class ProductMapperTest {

    @Test
    void publicResponseExcludesImei() {
        Product product = productWithImei();
        ProductVariant variant = variantWithImei(product);

        ProductPublicResponse response = ProductMapper.toPublicResponse(
                product, List.of(variant), Map.of(), List.of(), List.of());

        assertThat(response.getClass().getRecordComponents())
                .extracting(component -> component.getName())
                .doesNotContain("imei");
        assertThat(response.variants().getFirst().getClass().getRecordComponents())
                .extracting(component -> component.getName())
                .doesNotContain("imei");
        assertThat(response.name()).isEqualTo("iPhone 14");
        assertThat(response.seoTitle()).isEqualTo("iPhone 14 Uruguay");
        assertThat(response.metaDescription()).isEqualTo("iPhone 14 usado en WA Shop");
        assertThat(response.indexable()).isTrue();
    }

    @Test
    void publicImageResponseExcludesPublicId() {
        Product product = productWithImei();
        ProductVariant variant = variantWithImei(product);
        uy.washop.product.domain.ProductImage image = new uy.washop.product.domain.ProductImage();
        image.setUrl("https://cdn.example/a.jpg");
        image.setPublicId("cloud/secret-id");
        image.setAltText("Front");
        image.setPosition(0);
        image.setMainImage(true);

        ProductPublicResponse response = ProductMapper.toPublicResponse(
                product, List.of(variant), Map.of(variant.getId(), List.of(image)), List.of(), List.of());

        assertThat(response.variants().getFirst().images()).hasSize(1);
        assertThat(response.variants().getFirst().images().getFirst().getClass().getRecordComponents())
                .extracting(component -> component.getName())
                .doesNotContain("publicId");
        assertThat(response.variants().getFirst().images().getFirst().url()).isEqualTo("https://cdn.example/a.jpg");
    }

    @Test
    void adminResponseIncludesImeiOnVariant() {
        Product product = productWithImei();
        ProductVariant variant = variantWithImei(product);

        ProductAdminResponse response = ProductMapper.toAdminResponse(
                product, List.of(variant), Map.of(), List.of(), List.of());

        assertThat(response.variants().getFirst().imei()).isEqualTo("356938035643809");
    }

    private Product productWithImei() {
        Product product = new Product();
        product.setSlug("iphone-14");
        product.setName("iPhone 14");
        product.setProductType(ProductType.IPHONE);
        product.setCondition(ProductCondition.USED);
        product.setPrice(new BigDecimal("28990.00"));
        product.setCurrency(CurrencyCode.UYU);
        product.setStock(1);
        product.setImei("356938035643809");
        product.setBatteryHealth(92);
        product.setSeoTitle("iPhone 14 Uruguay");
        product.setMetaDescription("iPhone 14 usado en WA Shop");
        product.setIndexable(true);
        return product;
    }

    private ProductVariant variantWithImei(Product product) {
        ProductVariant variant = new ProductVariant();
        variant.setId(java.util.UUID.fromString("11111111-1111-1111-1111-111111111111"));
        variant.setProduct(product);
        variant.setCondition(ProductCondition.USED);
        variant.setPrice(new BigDecimal("28990.00"));
        variant.setCurrency(CurrencyCode.UYU);
        variant.setStock(1);
        variant.setImei("356938035643809");
        variant.setBatteryHealth(92);
        variant.setPublished(true);
        return variant;
    }
}
