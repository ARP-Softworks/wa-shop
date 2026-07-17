package uy.washop.seo.api.dto;

import java.util.List;

public record SeoPageMetaResponse(
        String title,
        String description,
        String canonical,
        String robots,
        String ogImage,
        String ogImageAlt,
        List<String> jsonLd,
        boolean notFound
) {
}
