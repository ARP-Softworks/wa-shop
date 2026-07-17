package uy.washop.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;
import uy.washop.config.AppProperties;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AppProperties appProperties;
    private final JsonSecurityHandlers jsonSecurityHandlers;

    public SecurityConfig(AppProperties appProperties, JsonSecurityHandlers jsonSecurityHandlers) {
        this.appProperties = appProperties;
        this.jsonSecurityHandlers = jsonSecurityHandlers;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookieCustomizer(cookie -> cookie
                .path("/")
                .secure(appProperties.getCookie().isSecure())
                .sameSite(appProperties.getCookie().getSameSite())
        );

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new AngularCsrfTokenRequestHandler())
                        // Mercado Pago calls this server-to-server (no browser, no CSRF cookie).
                        // Authenticity is instead verified via x-signature HMAC in the controller.
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/api/public/mercadopago/webhook", "POST")
                        )
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonSecurityHandlers)
                        .accessDeniedHandler(jsonSecurityHandlers)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/actuator/health", "/api/actuator/info").permitAll()
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/robots.txt", "/sitemap.xml").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterAfter(csrfCookieFilter(), UsernamePasswordAuthenticationFilter.class);

        if (appProperties.getCors().isEnabled()) {
            http.cors(Customizer.withDefaults());
        } else {
            http.cors(AbstractHttpConfigurer::disable);
        }

        return http.build();
    }

    @Bean
    OncePerRequestFilter csrfCookieFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain
            ) throws ServletException, IOException {
                CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                if (csrfToken != null) {
                    csrfToken.getToken();
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(appProperties.getCors().getAllowedOrigins().split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
