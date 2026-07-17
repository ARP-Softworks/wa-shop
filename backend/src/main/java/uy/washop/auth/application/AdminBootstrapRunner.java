package uy.washop.auth.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uy.washop.auth.domain.User;
import uy.washop.auth.domain.UserRole;
import uy.washop.auth.infrastructure.UserRepository;
import uy.washop.config.AppProperties;

/**
 * Creates an initial admin only when bootstrap env vars are set and the email does not exist.
 * Credentials must come from environment — never hardcode secrets in the repository.
 */
@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrapRunner(
            AppProperties appProperties,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appProperties = appProperties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        String email = appProperties.getAdminBootstrap().getEmail();
        String password = appProperties.getAdminBootstrap().getPassword();

        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            return;
        }

        if (userRepository.existsByEmailIgnoreCase(email.trim())) {
            log.info("Admin bootstrap skipped: user already exists");
            return;
        }

        User user = new User();
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName("Admin");
        user.setLastName("Bootstrap");
        user.setRole(UserRole.ADMIN);
        user.setEnabled(true);
        userRepository.save(user);

        log.info("Admin bootstrap user created from environment configuration");
    }
}
