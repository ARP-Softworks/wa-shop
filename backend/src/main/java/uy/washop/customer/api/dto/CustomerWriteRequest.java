package uy.washop.customer.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerWriteRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 40) String phone,
        @Email @Size(max = 320) String email,
        String address,
        String notes
) {
}
