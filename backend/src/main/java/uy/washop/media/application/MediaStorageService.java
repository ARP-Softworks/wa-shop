package uy.washop.media.application;

import java.io.InputStream;
import uy.washop.media.domain.StoredMedia;

/**
 * Abstraction over image storage providers (Cloudinary, local filesystem, etc.).
 * Domain and controllers depend on this interface only.
 */
public interface MediaStorageService {

    String providerName();

    StoredMedia store(InputStream content, long sizeBytes, String contentType, String safeFilename, String folder);

    void delete(String publicId);
}
