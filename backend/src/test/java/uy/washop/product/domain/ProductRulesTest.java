package uy.washop.product.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import uy.washop.shared.domain.CurrencyCode;
import uy.washop.shared.exception.BusinessConflictException;

class ProductRulesTest {

    @Test
    void allowsBatteryHealthOnUsedIphone() {
        Product product = baseProduct(ProductType.IPHONE, ProductCondition.USED);
        product.setBatteryHealth(90);

        assertThatCode(() -> ProductRules.validateAll(product)).doesNotThrowAnyException();
    }

    @Test
    void rejectsBatteryHealthOnNewIphone() {
        Product product = baseProduct(ProductType.IPHONE, ProductCondition.NEW);
        product.setBatteryHealth(90);

        assertThatThrownBy(() -> ProductRules.validateBatteryHealth(product))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("batería");
    }

    @Test
    void rejectsBatteryHealthOnAccessory() {
        Product product = baseProduct(ProductType.ACCESSORY, ProductCondition.NEW);
        product.setBatteryHealth(80);

        assertThatThrownBy(() -> ProductRules.validateBatteryHealth(product))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void rejectsPreviousPriceNotGreaterThanCurrent() {
        Product product = baseProduct(ProductType.IPHONE, ProductCondition.USED);
        product.setPrice(new BigDecimal("1000.00"));
        product.setPreviousPrice(new BigDecimal("1000.00"));

        assertThatThrownBy(() -> ProductRules.validatePreviousPrice(product))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("precio anterior");
    }

    private Product baseProduct(ProductType type, ProductCondition condition) {
        Product product = new Product();
        product.setSlug("test-product");
        product.setName("Test");
        product.setProductType(type);
        product.setCondition(condition);
        product.setPrice(new BigDecimal("1000.00"));
        product.setCurrency(CurrencyCode.UYU);
        product.setStock(1);
        return product;
    }
}
