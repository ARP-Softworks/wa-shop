package uy.washop.discount.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uy.washop.config.AppProperties;
import uy.washop.shared.exception.RateLimitExceededException;

@Service
public class DiscountValidateRateLimitService {

    private final AppProperties appProperties;
    private final ConcurrentHashMap<String, List<Instant>> attemptsByKey = new ConcurrentHashMap<>();

    public DiscountValidateRateLimitService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public void assertAllowed(String clientIp) {
        String key = "ip:" + normalizeIp(clientIp);
        int count = prune(key);
        if (count >= appProperties.getOrder().getDiscountValidateIpMaxPerWindow()) {
            throw new RateLimitExceededException(
                    "Demasiados intentos con códigos. Esperá unos minutos e intentá de nuevo."
            );
        }
    }

    public void recordAttempt(String clientIp) {
        Instant now = Instant.now();
        String key = "ip:" + normalizeIp(clientIp);
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
}
