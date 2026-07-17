package uy.washop.media.api.dto;

import jakarta.validation.constraints.NotBlank;

public record MediaDeleteRequest(@NotBlank String publicId) {
}
