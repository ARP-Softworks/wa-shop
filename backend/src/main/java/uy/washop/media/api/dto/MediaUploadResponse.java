package uy.washop.media.api.dto;

public record MediaUploadResponse(
        String url,
        String publicId,
        String provider,
        String format,
        String contentType,
        long sizeBytes,
        Integer width,
        Integer height
) {
}
