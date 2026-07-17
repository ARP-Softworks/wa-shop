package uy.washop.media.domain;

/**
 * Result of storing a file in an external or local media provider.
 * Binary content is never persisted in PostgreSQL.
 */
public record StoredMedia(
        String url,
        String publicId,
        String format,
        String contentType,
        long sizeBytes,
        Integer width,
        Integer height
) {
}
