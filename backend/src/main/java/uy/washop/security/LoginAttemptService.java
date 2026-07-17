package uy.washop.security;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uy.washop.config.AppProperties;

/**
 * Basic failed-login tracking. Structure ready to swap for distributed rate limiting.
 */
@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private final AppProperties appProperties;
    private final ConcurrentHashMap<String, AttemptState> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public boolean isBlocked(String email) {
        AttemptState state = attempts.get(normalize(email));
        if (state == null || state.lockedUntil == null) {
            return false;
        }
        if (Instant.now().isAfter(state.lockedUntil)) {
            attempts.remove(normalize(email));
            return false;
        }
        return true;
    }

    public void loginSucceeded(String email) {
        attempts.remove(normalize(email));
    }

    public void loginFailed(String email) {
        String key = normalize(email);
        AttemptState state = attempts.compute(key, (ignored, current) -> {
            AttemptState next = current == null ? new AttemptState() : current;
            next.failures += 1;
            if (next.failures >= appProperties.getLogin().getMaxFailedAttempts()) {
                next.lockedUntil = Instant.now()
                        .plusSeconds(appProperties.getLogin().getLockDurationSeconds());
                log.warn("Temporary login lock applied for an account after repeated failures");
            }
            return next;
        });
        log.debug("Recorded failed login attempt (count={})", state.failures);
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static final class AttemptState {
        private int failures;
        private Instant lockedUntil;
    }
}
