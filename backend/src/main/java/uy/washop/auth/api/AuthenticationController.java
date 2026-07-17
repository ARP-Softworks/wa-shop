package uy.washop.auth.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.auth.api.dto.AuthenticatedUserResponse;
import uy.washop.auth.api.dto.CsrfTokenResponse;
import uy.washop.auth.api.dto.LoginRequest;
import uy.washop.auth.application.AuthenticationService;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/csrf")
    public CsrfTokenResponse csrf(CsrfToken csrfToken) {
        return new CsrfTokenResponse(
                csrfToken.getToken(),
                csrfToken.getHeaderName(),
                csrfToken.getParameterName()
        );
    }

    @PostMapping("/login")
    public AuthenticatedUserResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        return authenticationService.login(request, httpRequest, httpResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public AuthenticatedUserResponse me() {
        return authenticationService.currentUser();
    }
}
