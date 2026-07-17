package uy.washop.product.api.dto;

import java.util.UUID;

public record ProductFeatureResponse(
        UUID id,
        String name,
        String value
) {
}
