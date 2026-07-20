package uy.washop.promotion.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import uy.washop.promotion.domain.Promotion;

/**
 * Cross-category "buy N from group A, get M from group B at X% off" pricing. A promotion whose
 * trigger and reward categories are the same covers mix-and-match bundles (any 3 units from a
 * category, cheapest one discounted); different categories cover cross-sell gifts (2 phones,
 * 2 accessories discounted).
 *
 * <p>Each promotion is evaluated independently against the full cart, so two active promotions
 * that reward the same category can both discount the same cheapest units — an accepted v1
 * limitation rather than an attempt at globally-optimal, non-overlapping allocation.
 */
public final class PromotionEngine {

    private PromotionEngine() {
    }

    /** Returns each line's discount, keyed by {@link CategoryCartLine#key()}. Zero-discount lines are omitted. */
    public static Map<UUID, BigDecimal> calculateDiscounts(
            List<CategoryCartLine> lines, List<Promotion> promotions
    ) {
        Map<UUID, BigDecimal> discounts = new LinkedHashMap<>();
        for (Promotion promotion : promotions) {
            int triggerUnits = lines.stream()
                    .filter(line -> line.categoryId().equals(promotion.getTriggerCategoryId()))
                    .mapToInt(CategoryCartLine::quantity)
                    .sum();
            int multiplier = triggerUnits / promotion.getTriggerQuantity();
            if (multiplier == 0) {
                continue;
            }

            int rewardUnitsRemaining = multiplier * promotion.getRewardQuantity();
            BigDecimal discountFraction = BigDecimal.valueOf(promotion.getDiscountPercent())
                    .divide(BigDecimal.valueOf(100));

            List<CategoryCartLine> rewardLines = lines.stream()
                    .filter(line -> line.categoryId().equals(promotion.getRewardCategoryId()))
                    .sorted(Comparator.comparing(CategoryCartLine::effectiveUnitPrice))
                    .toList();

            for (CategoryCartLine line : rewardLines) {
                if (rewardUnitsRemaining <= 0) {
                    break;
                }
                int unitsHere = Math.min(rewardUnitsRemaining, line.quantity());
                BigDecimal lineDiscount = line.effectiveUnitPrice()
                        .multiply(BigDecimal.valueOf(unitsHere))
                        .multiply(discountFraction)
                        .setScale(2, RoundingMode.HALF_UP);
                discounts.merge(line.key(), lineDiscount, BigDecimal::add);
                rewardUnitsRemaining -= unitsHere;
            }
        }
        return discounts;
    }

    public static BigDecimal totalDiscount(Map<UUID, BigDecimal> discounts) {
        return discounts.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
