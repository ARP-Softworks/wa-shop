package uy.washop.product.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import uy.washop.product.api.dto.ProductAdminResponse;
import uy.washop.product.api.dto.ProductPublicResponse;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.shared.domain.CurrencyCode;

class ProductMapperTest {

    @Test
    void publicResponseExcludesImei() {
        Product product = productWithImei();

        ProductPublicResponse response = ProductMapper.toPublicResponse(product, List.of(), List.of());

        assertThat(response.getClass().getRecordComponents())
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
        uy.washop.product.domain.ProductImage image = new uy.washop.product.domain.ProductImage();
        image.setUrl("https://cdn.example/a.jpg");
        image.setPublicId("cloud/secret-id");
        image.setAltText("Front");
        image.setPosition(0);
        image.setMainImage(true);

        ProductPublicResponse response = ProductMapper.toPublicResponse(product, List.of(image), List.of());

        assertThat(response.images()).hasSize(1);
        assertThat(response.images().getFirst().getClass().getRecordComponents())
                .extracting(component -> component.getName())
                .doesNotContain("publicId");
        assertThat(response.images().getFirst().url()).isEqualTo("https://cdn.example/a.jpg");
    }

    @Test
    void adminResponseIncludesImei() {
        Product product = productWithImei();

        ProductAdminResponse response = ProductMapper.toAdminResponse(product, List.of(), List.of());

        assertThat(response.imei()).isEqualTo("356938035643809");
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
}
