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
        assertThatThrownBy(() -> ProductRules.validateBatteryHealth(
                        ProductType.IPHONE, ProductCondition.NEW, 90))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("batería");
    }

    @Test
    void rejectsBatteryHealthOnAccessory() {
        assertThatThrownBy(() -> ProductRules.validateBatteryHealth(
                        ProductType.ACCESSORY, ProductCondition.NEW, 80))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void requiresBatteryHealthOnUsedIphone() {
        assertThatThrownBy(() -> ProductRules.validateBatteryHealth(
                        ProductType.IPHONE, ProductCondition.USED, null))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("obligatoria");
    }

    @Test
    void rejectsPreviousPriceNotGreaterThanCurrent() {
        assertThatThrownBy(() -> ProductRules.validatePreviousPrice(
                        new BigDecimal("1000.00"), new BigDecimal("1000.00")))
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
