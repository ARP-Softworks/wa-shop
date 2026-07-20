package uy.washop.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Password is required on create; on update, leave blank to keep the current password. */
public record UserWriteRequest(
        @NotBlank @Email @Size(max = 320) String email,
        @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        boolean enabled
) {
}
