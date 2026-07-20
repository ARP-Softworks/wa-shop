package uy.washop.promotion.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uy.washop.category.domain.Category;
import uy.washop.promotion.domain.Promotion;

class PromotionEngineTest {

    private static final UUID CASES_ID = UUID.randomUUID();
    private static final UUID PHONES_ID = UUID.randomUUID();
    private static final UUID ACCESSORIES_ID = UUID.randomUUID();

    @Test
    void mixAndMatchDiscountsCheapestUnitAcrossDifferentProducts() {
        // 3x2-style mix-and-match: 3 different cases, cheapest one goes free.
        Promotion promo = promotion(CASES_ID, 3, CASES_ID, 1, 100);
        UUID caseA = UUID.randomUUID();
        UUID caseB = UUID.randomUUID();
        List<CategoryCartLine> lines = List.of(
                new CategoryCartLine(caseA, CASES_ID, 2, new BigDecimal("15.00")),
                new CategoryCartLine(caseB, CASES_ID, 1, new BigDecimal("10.00"))
        );

        Map<UUID, BigDecimal> discounts = PromotionEngine.calculateDiscounts(lines, List.of(promo));

        assertThat(discounts).containsOnly(Map.entry(caseB, new BigDecimal("10.00")));
        assertThat(PromotionEngine.totalDiscount(discounts)).isEqualByComparingTo("10.00");
    }

    @Test
    void crossCategoryGiftDiscountsCheapestAccessories() {
        // Buy 2 phones, get 2 accessories at 50% off.
        Promotion promo = promotion(PHONES_ID, 2, ACCESSORIES_ID, 2, 50);
        UUID phone = UUID.randomUUID();
        UUID cheapAccessory = UUID.randomUUID();
        UUID pricierAccessory = UUID.randomUUID();
        List<CategoryCartLine> lines = List.of(
                new CategoryCartLine(phone, PHONES_ID, 2, new BigDecimal("500.00")),
                new CategoryCartLine(cheapAccessory, ACCESSORIES_ID, 1, new BigDecimal("20.00")),
                new CategoryCartLine(pricierAccessory, ACCESSORIES_ID, 2, new BigDecimal("30.00"))
        );

        Map<UUID, BigDecimal> discounts = PromotionEngine.calculateDiscounts(lines, List.of(promo));

        // Cheapest 2 accessory units: the 1 unit at 20 + 1 of the 2 units at 30, each at 50% off.
        assertThat(discounts.get(cheapAccessory)).isEqualByComparingTo("10.00");
        assertThat(discounts.get(pricierAccessory)).isEqualByComparingTo("15.00");
        assertThat(PromotionEngine.totalDiscount(discounts)).isEqualByComparingTo("25.00");
    }

    @Test
    void belowTriggerThresholdAppliesNoDiscount() {
        Promotion promo = promotion(PHONES_ID, 2, ACCESSORIES_ID, 1, 100);
        UUID phone = UUID.randomUUID();
        UUID accessory = UUID.randomUUID();
        List<CategoryCartLine> lines = List.of(
                new CategoryCartLine(phone, PHONES_ID, 1, new BigDecimal("500.00")),
                new CategoryCartLine(accessory, ACCESSORIES_ID, 1, new BigDecimal("20.00"))
        );

        Map<UUID, BigDecimal> discounts = PromotionEngine.calculateDiscounts(lines, List.of(promo));

        assertThat(discounts).isEmpty();
    }

    @Test
    void discountCappedByAvailableRewardStock() {
        // Trigger unlocks 3 reward units, but only 1 accessory is in the cart.
        Promotion promo = promotion(PHONES_ID, 1, ACCESSORIES_ID, 3, 100);
        UUID phone = UUID.randomUUID();
        UUID accessory = UUID.randomUUID();
        List<CategoryCartLine> lines = List.of(
                new CategoryCartLine(phone, PHONES_ID, 1, new BigDecimal("500.00")),
                new CategoryCartLine(accessory, ACCESSORIES_ID, 1, new BigDecimal("20.00"))
        );

        Map<UUID, BigDecimal> discounts = PromotionEngine.calculateDiscounts(lines, List.of(promo));

        assertThat(PromotionEngine.totalDiscount(discounts)).isEqualByComparingTo("20.00");
    }

    @Test
    void multiplierScalesRewardWithExtraTriggerBundles() {
        // 6 phones with trigger quantity 3 = two bundles, each unlocking 1 free accessory.
        Promotion promo = promotion(PHONES_ID, 3, ACCESSORIES_ID, 1, 100);
        UUID phone = UUID.randomUUID();
        UUID accessoryA = UUID.randomUUID();
        UUID accessoryB = UUID.randomUUID();
        List<CategoryCartLine> lines = List.of(
                new CategoryCartLine(phone, PHONES_ID, 6, new BigDecimal("500.00")),
                new CategoryCartLine(accessoryA, ACCESSORIES_ID, 1, new BigDecimal("15.00")),
                new CategoryCartLine(accessoryB, ACCESSORIES_ID, 1, new BigDecimal("25.00"))
        );

        Map<UUID, BigDecimal> discounts = PromotionEngine.calculateDiscounts(lines, List.of(promo));

        assertThat(PromotionEngine.totalDiscount(discounts)).isEqualByComparingTo("40.00");
    }

    private static Promotion promotion(
            UUID triggerCategoryId, int triggerQuantity, UUID rewardCategoryId, int rewardQuantity, int discountPercent
    ) {
        Promotion promotion = new Promotion();
        promotion.setName("Test promo");
        promotion.setActive(true);
        promotion.setTriggerCategory(categoryWithId(triggerCategoryId));
        promotion.setTriggerQuantity(triggerQuantity);
        promotion.setRewardCategory(categoryWithId(rewardCategoryId));
        promotion.setRewardQuantity(rewardQuantity);
        promotion.setDiscountPercent(discountPercent);
        return promotion;
    }

    private static Category categoryWithId(UUID id) {
        Category category = new Category();
        category.setId(id);
        return category;
    }
}
