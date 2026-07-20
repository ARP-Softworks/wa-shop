package uy.washop.order.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import uy.washop.product.domain.Product;

class CheckoutServicePromotionTest {

    @Test
    void chargesFullPriceWithoutPromotion() {
        Product product = product("100.00", null, null);

        assertThat(CheckoutService.calculateLineSubtotal(product, 3)).isEqualByComparingTo("300.00");
    }

    @Test
    void appliesTwoForOneExactly() {
        Product product = product("100.00", 2, 1);

        assertThat(CheckoutService.calculateLineSubtotal(product, 2)).isEqualByComparingTo("100.00");
        assertThat(CheckoutService.calculateLineSubtotal(product, 4)).isEqualByComparingTo("200.00");
    }

    @Test
    void appliesTwoForOneWithRemainder() {
        Product product = product("100.00", 2, 1);

        // 5 units = two 2x1 bundles (pay 1 each) + 1 full-price unit = 100 + 100 + 100 = 300
        assertThat(CheckoutService.calculateLineSubtotal(product, 5)).isEqualByComparingTo("300.00");
    }

    @Test
    void appliesThreeForTwoWithRemainder() {
        Product product = product("90.00", 3, 2);

        // 7 units = two 3x2 bundles (pay 2 each) + 1 full-price unit = (2*2 + 1) * 90 = 450
        assertThat(CheckoutService.calculateLineSubtotal(product, 7)).isEqualByComparingTo("450.00");
    }

    @Test
    void quantityBelowBundleSizeStillFullPrice() {
        Product product = product("100.00", 3, 2);

        assertThat(CheckoutService.calculateLineSubtotal(product, 1)).isEqualByComparingTo("100.00");
        assertThat(CheckoutService.calculateLineSubtotal(product, 2)).isEqualByComparingTo("200.00");
    }

    private static Product product(String price, Integer buy, Integer pay) {
        Product product = new Product();
        product.setPrice(new BigDecimal(price));
        product.setPromoBuyQuantity(buy);
        product.setPromoPayQuantity(pay);
        return product;
    }
}
