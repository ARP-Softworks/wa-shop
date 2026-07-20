package uy.washop.promotion.application;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One order line as seen by {@link PromotionEngine}: {@code key} identifies the line (the
 * product id) so discounts can be attributed back to it, {@code effectiveUnitPrice} is the
 * average per-unit price already reflecting any per-product "buy X pay Y" promo.
 */
public record CategoryCartLine(UUID key, UUID categoryId, int quantity, BigDecimal effectiveUnitPrice) {
}
