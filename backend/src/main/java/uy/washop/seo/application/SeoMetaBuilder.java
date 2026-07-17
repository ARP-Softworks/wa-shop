package uy.washop.seo.application;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SeoMetaBuilder {

    public String sanitizePlainText(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder cleaned = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '<' || c == '>') {
                continue;
            }
            if (Character.isISOControl(c) && c != '\n' && c != '\r' && c != '\t') {
                continue;
            }
            cleaned.append(c);
        }
        String result = cleaned.toString().replaceAll("\\s+", " ").trim();
        return result.isEmpty() ? null : result;
    }

    public String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String sanitized = sanitizePlainText(value);
            if (StringUtils.hasText(sanitized)) {
                return sanitized;
            }
        }
        return null;
    }

    public String truncate(String value, int maxLength) {
        String sanitized = sanitizePlainText(value);
        if (sanitized == null) {
            return null;
        }
        if (sanitized.length() <= maxLength) {
            return sanitized;
        }
        return sanitized.substring(0, maxLength).trim();
    }

    public String robotsDirective(boolean indexable, boolean noindexForced) {
        if (noindexForced || !indexable) {
            return "noindex, nofollow";
        }
        return "index, follow";
    }
}
