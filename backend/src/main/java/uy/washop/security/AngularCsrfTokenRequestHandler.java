package uy.washop.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

/**
 * CSRF request handler compatible with Angular cookie + header double-submit pattern.
 * Mirrors the Spring Security SPA guidance for CookieCsrfTokenRepository.
 */
public final class AngularCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestAttributeHandler plain = new CsrfTokenRequestAttributeHandler();
    private final XorCsrfTokenRequestAttributeHandler xor = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Supplier<CsrfToken> csrfToken
    ) {
        xor.handle(request, response, csrfToken);
        csrfToken.get();
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        String headerValue = request.getHeader(csrfToken.getHeaderName());
        return (StringUtils.hasText(headerValue) ? plain : xor)
                .resolveCsrfTokenValue(request, csrfToken);
    }
}
