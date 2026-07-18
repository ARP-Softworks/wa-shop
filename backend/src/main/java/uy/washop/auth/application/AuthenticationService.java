package uy.washop.auth.application;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.auth.api.dto.AuthenticatedUserResponse;
import uy.washop.auth.api.dto.ChangePasswordRequest;
import uy.washop.auth.api.dto.LoginRequest;
import uy.washop.auth.domain.User;
import uy.washop.auth.infrastructure.UserRepository;
import uy.washop.security.AdminUserDetails;
import uy.washop.security.LoginAttemptService;

@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private static final String GENERIC_LOGIN_ERROR = "Credenciales inválidas";

    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            LoginAttemptService loginAttemptService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService
    ) {
        this.authenticationManager = authenticationManager;
        this.loginAttemptService = loginAttemptService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public AuthenticatedUserResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String email = request.email().trim();

        if (loginAttemptService.isBlocked(email)) {
            log.warn("Login rejected due to temporary lock");
            throw new BadCredentialsException(GENERIC_LOGIN_ERROR);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password())
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);

            loginAttemptService.loginSucceeded(email);
            log.info("Admin login succeeded");

            return toResponse((AdminUserDetails) authentication.getPrincipal());
        } catch (AuthenticationException ex) {
            loginAttemptService.loginFailed(email);
            throw new BadCredentialsException(GENERIC_LOGIN_ERROR);
        }
    }

    public AuthenticatedUserResponse currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AdminUserDetails details)) {
            throw new BadCredentialsException("No autenticado");
        }
        return toResponse(details);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, authentication);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();
        log.info("Admin logout completed");
    }

    public void changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AdminUserDetails details)) {
            throw new BadCredentialsException("No autenticado");
        }

        User user = userRepository.findById(details.getUser().getId())
                .orElseThrow(() -> new BadCredentialsException("No autenticado"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("La contraseña actual no es correcta");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        auditService.record(AuditAction.UPDATE, "User", user.getId(), "Contraseña actualizada");
        log.info("Admin password changed");
    }

    private AuthenticatedUserResponse toResponse(AdminUserDetails details) {
        User user = details.getUser();
        return new AuthenticatedUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }
}
