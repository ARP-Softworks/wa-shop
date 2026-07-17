package uy.washop.seo.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.settings.domain.SiteSettings;
import uy.washop.shared.domain.CurrencyCode;

@SpringBootTest
@ActiveProfiles("test")
class JsonLdBuilderTest {

    @Autowired
    private JsonLdBuilder jsonLdBuilder;

    @Test
    void productJsonLdDoesNotIncludeImei() {
        Product product = new Product();
        product.setSlug("iphone-14");
        product.setName("iPhone 14");
        product.setDescription("Equipo usado");
        product.setProductType(ProductType.IPHONE);
        product.setCondition(ProductCondition.USED);
        product.setPrice(new BigDecimal("28990.00"));
        product.setCurrency(CurrencyCode.UYU);
        product.setStock(1);
        product.setImei("356938035643809");
        product.setBatteryHealth(92);

        SiteSettings settings = new SiteSettings();
        settings.setBusinessName("WA Shop");

        String json = jsonLdBuilder.product(product, "https://cdn.example/a.jpg", settings);

        assertThat(json).doesNotContain("356938035643809");
        assertThat(json).doesNotContainIgnoringCase("imei");
        assertThat(json).contains("\"@type\":\"Product\"");
        assertThat(json).contains("iPhone 14");
    }
}
