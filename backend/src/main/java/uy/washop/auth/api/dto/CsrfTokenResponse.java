package uy.washop.auth.api.dto;

public record CsrfTokenResponse(String token, String headerName, String cookieName) {
}
