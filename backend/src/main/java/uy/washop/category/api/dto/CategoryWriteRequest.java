package uy.washop.category.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryWriteRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 140) String slug,
        String description,
        boolean active,
        @Size(max = 70) String seoTitle,
        @Size(max = 320) String metaDescription,
        Boolean indexable
) {
}
