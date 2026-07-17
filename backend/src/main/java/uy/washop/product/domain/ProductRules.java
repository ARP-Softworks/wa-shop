package uy.washop.product.domain;

import uy.washop.shared.exception.BusinessConflictException;

public final class ProductRules {

    private ProductRules() {
    }

    public static void validateBatteryHealth(Product product) {
        Integer batteryHealth = product.getBatteryHealth();
        boolean usedIphone = product.getProductType() == ProductType.IPHONE
                && product.getCondition() == ProductCondition.USED;

        if (batteryHealth != null && !usedIphone) {
            throw new BusinessConflictException(
                    "La salud de batería solo aplica a iPhone usados"
            );
        }
    }

    public static void validatePreviousPrice(Product product) {
        if (product.getPreviousPrice() != null
                && product.getPrice() != null
                && product.getPreviousPrice().compareTo(product.getPrice()) <= 0) {
            throw new BusinessConflictException(
                    "El precio anterior debe ser mayor al precio actual"
            );
        }
    }

    public static void validateAll(Product product) {
        validateBatteryHealth(product);
        validatePreviousPrice(product);
    }
}
