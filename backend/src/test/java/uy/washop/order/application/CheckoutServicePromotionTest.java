package uy.washop.order.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductVariant;
import uy.washop.shared.domain.CurrencyCode;

class CheckoutServicePromotionTest {

    @Test
    void chargesFullPriceWithoutPromotion() {
        assertThat(CheckoutService.calculateLineSubtotal(product(null, null), variant("100.00"), 3))
                .isEqualByComparingTo("300.00");
    }

    @Test
    void appliesTwoForOneExactly() {
        Product product = product(2, 1);

        assertThat(CheckoutService.calculateLineSubtotal(product, variant("100.00"), 2))
                .isEqualByComparingTo("100.00");
        assertThat(CheckoutService.calculateLineSubtotal(product, variant("100.00"), 4))
                .isEqualByComparingTo("200.00");
    }

    @Test
    void appliesTwoForOneWithRemainder() {
        Product product = product(2, 1);

        assertThat(CheckoutService.calculateLineSubtotal(product, variant("100.00"), 5))
                .isEqualByComparingTo("300.00");
    }

    @Test
    void appliesThreeForTwoWithRemainder() {
        Product product = product(3, 2);

        assertThat(CheckoutService.calculateLineSubtotal(product, variant("90.00"), 7))
                .isEqualByComparingTo("450.00");
    }

    @Test
    void quantityBelowBundleSizeStillFullPrice() {
        Product product = product(3, 2);

        assertThat(CheckoutService.calculateLineSubtotal(product, variant("100.00"), 1))
                .isEqualByComparingTo("100.00");
        assertThat(CheckoutService.calculateLineSubtotal(product, variant("100.00"), 2))
                .isEqualByComparingTo("200.00");
    }

    private static Product product(Integer buy, Integer pay) {
        Product product = new Product();
        product.setPromoBuyQuantity(buy);
        product.setPromoPayQuantity(pay);
        return product;
    }

    private static ProductVariant variant(String price) {
        ProductVariant variant = new ProductVariant();
        variant.setPrice(new BigDecimal(price));
        variant.setCurrency(CurrencyCode.UYU);
        return variant;
    }
}
