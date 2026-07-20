package uy.washop.product.domain;

import java.math.BigDecimal;
import uy.washop.shared.exception.BusinessConflictException;

public final class ProductRules {

    private ProductRules() {
    }

    public static void validateBatteryHealth(ProductType productType, ProductCondition condition, Integer batteryHealth) {
        boolean usedIphone = productType == ProductType.IPHONE && condition == ProductCondition.USED;

        if (batteryHealth != null && !usedIphone) {
            throw new BusinessConflictException(
                    "La salud de batería solo aplica a iPhone usados"
            );
        }
        if (usedIphone && batteryHealth == null) {
            throw new BusinessConflictException(
                    "La salud de batería es obligatoria para iPhone usados"
            );
        }
    }

    public static void validatePreviousPrice(BigDecimal price, BigDecimal previousPrice) {
        if (previousPrice != null
                && price != null
                && previousPrice.compareTo(price) <= 0) {
            throw new BusinessConflictException(
                    "El precio anterior debe ser mayor al precio actual"
            );
        }
    }

    public static void validatePromotion(Integer buy, Integer pay) {
        if (buy == null && pay == null) {
            return;
        }
        if (buy == null || pay == null) {
            throw new BusinessConflictException(
                    "La promoción necesita cantidad a llevar y cantidad a pagar"
            );
        }
        if (pay < 1 || buy <= pay) {
            throw new BusinessConflictException(
                    "La promoción debe llevar más unidades de las que se pagan (ej. 2x1, 3x2)"
            );
        }
    }

    public static void validateVariant(
            ProductType productType,
            ProductCondition condition,
            Integer batteryHealth,
            BigDecimal price,
            BigDecimal previousPrice
    ) {
        validateBatteryHealth(productType, condition, batteryHealth);
        validatePreviousPrice(price, previousPrice);
    }

    public static void validateAll(Product product) {
        validateBatteryHealth(product.getProductType(), product.getCondition(), product.getBatteryHealth());
        validatePreviousPrice(product.getPrice(), product.getPreviousPrice());
        validatePromotion(product.getPromoBuyQuantity(), product.getPromoPayQuantity());
    }
}
