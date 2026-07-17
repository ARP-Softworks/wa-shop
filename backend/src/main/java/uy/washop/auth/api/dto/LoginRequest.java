package uy.washop.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(min = 6, max = 100) String password
) {
}
