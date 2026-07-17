package uy.washop.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import uy.washop.auth.domain.User;
import uy.washop.auth.domain.UserRole;
import uy.washop.config.ApplicationConfig;

@DataJpaTest
@ActiveProfiles("test")
@EntityScan("uy.washop")
@EnableJpaRepositories("uy.washop")
@Import(ApplicationConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void storesBcryptPasswordHashNotPlainText() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "ChangeMe123!";

        User user = new User();
        user.setEmail("repo-test@washop.uy");
        user.setPasswordHash(encoder.encode(rawPassword));
        user.setFirstName("Repo");
        user.setLastName("Test");
        user.setRole(UserRole.ADMIN);
        user.setEnabled(true);

        User saved = userRepository.saveAndFlush(user);

        assertThat(saved.getPasswordHash()).startsWith("$2a$");
        assertThat(saved.getPasswordHash()).isNotEqualTo(rawPassword);
        assertThat(encoder.matches(rawPassword, saved.getPasswordHash())).isTrue();
        assertThat(userRepository.findByEmailIgnoreCase("REPO-TEST@washop.uy")).isPresent();
    }
}
