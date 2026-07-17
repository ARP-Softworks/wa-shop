package uy.washop.shared.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiErrorResponseTest {

    @Test
    void holdsConsistentErrorShape() {
        ApiErrorResponse response = new ApiErrorResponse(
                java.time.Instant.parse("2026-01-01T00:00:00Z"),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Error de validación",
                "/api/example",
                java.util.List.of("name: no debe estar vacío")
        );

        assertThat(response.status()).isEqualTo(400);
        assertThat(response.error()).isEqualTo("Bad Request");
        assertThat(response.message()).isEqualTo("Error de validación");
        assertThat(response.path()).isEqualTo("/api/example");
        assertThat(response.details()).containsExactly("name: no debe estar vacío");
    }
}
