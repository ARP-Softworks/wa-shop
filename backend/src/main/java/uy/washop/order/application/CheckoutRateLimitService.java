package uy.washop.order.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uy.washop.config.AppProperties;
import uy.washop.shared.exception.RateLimitExceededException;

/**
 * In-memory rate limiting for public checkout. Ready to swap for a distributed store later.
 */
@Service
public class CheckoutRateLimitService {

    private final AppProperties appProperties;
    private final ConcurrentHashMap<String, List<Instant>> attemptsByKey = new ConcurrentHashMap<>();

    public CheckoutRateLimitService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public void assertAllowed(String clientIp, String customerPhone) {
        pruneAndCount("ip:" + normalizeIp(clientIp), appProperties.getOrder().getCheckoutIpMaxPerWindow());
        pruneAndCount("phone:" + normalizePhone(customerPhone), appProperties.getOrder().getCheckoutPhoneMaxPerWindow());
    }

    public void recordAttempt(String clientIp, String customerPhone) {
        Instant now = Instant.now();
        record("ip:" + normalizeIp(clientIp), now);
        record("phone:" + normalizePhone(customerPhone), now);
    }

    private void pruneAndCount(String key, int maxPerWindow) {
        int count = prune(key);
        if (count >= maxPerWindow) {
            throw new RateLimitExceededException(
                    "Demasiados intentos de compra. Esperá unos minutos e intentá de nuevo."
            );
        }
    }

    private void record(String key, Instant now) {
        attemptsByKey.compute(key, (ignored, current) -> {
            List<Instant> next = current == null ? new ArrayList<>() : new ArrayList<>(current);
            Instant cutoff = now.minusSeconds(appProperties.getOrder().getCheckoutWindowSeconds());
            next.removeIf(ts -> ts.isBefore(cutoff));
            next.add(now);
            return next;
        });
    }

    private int prune(String key) {
        Instant cutoff = Instant.now().minusSeconds(appProperties.getOrder().getCheckoutWindowSeconds());
        List<Instant> timestamps = attemptsByKey.compute(key, (ignored, current) -> {
            if (current == null || current.isEmpty()) {
                return new ArrayList<>();
            }
            List<Instant> next = new ArrayList<>(current);
            Iterator<Instant> it = next.iterator();
            while (it.hasNext()) {
                if (it.next().isBefore(cutoff)) {
                    it.remove();
                }
            }
            return next;
        });
        return timestamps.size();
    }

    private static String normalizeIp(String clientIp) {
        if (!StringUtils.hasText(clientIp)) {
            return "unknown";
        }
        return clientIp.trim().toLowerCase();
    }

    private static String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return "unknown";
        }
        return phone.replaceAll("\\D", "");
    }
}
