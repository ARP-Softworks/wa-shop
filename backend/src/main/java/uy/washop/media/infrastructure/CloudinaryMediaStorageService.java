package uy.washop.media.infrastructure;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uy.washop.config.AppProperties;
import uy.washop.media.application.MediaStorageService;
import uy.washop.media.domain.StoredMedia;
import uy.washop.shared.exception.BusinessConflictException;

@Service
@ConditionalOnProperty(name = "app.media.provider", havingValue = "cloudinary")
public class CloudinaryMediaStorageService implements MediaStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryMediaStorageService.class);

    private final Cloudinary cloudinary;

    public CloudinaryMediaStorageService(AppProperties appProperties) {
        AppProperties.Media.Cloudinary props = appProperties.getMedia().getCloudinary();
        if (!StringUtils.hasText(props.getCloudName())
                || !StringUtils.hasText(props.getApiKey())
                || !StringUtils.hasText(props.getApiSecret())) {
            throw new IllegalStateException(
                    "Cloudinary requires CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY and CLOUDINARY_API_SECRET"
            );
        }
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", props.getCloudName(),
                "api_key", props.getApiKey(),
                "api_secret", props.getApiSecret(),
                "secure", true
        ));
        log.info("Cloudinary media storage enabled for cloud={}", props.getCloudName());
    }

    @Override
    public String providerName() {
        return "cloudinary";
    }

    @Override
    @SuppressWarnings("unchecked")
    public StoredMedia store(
            InputStream content,
            long sizeBytes,
            String contentType,
            String safeFilename,
            String folder
    ) {
        try {
            byte[] bytes = content.readAllBytes();
            Map<String, Object> result = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                    "folder", "wa-shop/" + folder,
                    "public_id", stripExtension(safeFilename),
                    "resource_type", "image",
                    "overwrite", false,
                    "unique_filename", true
            ));
            String url = stringValue(result.get("secure_url"));
            if (!StringUtils.hasText(url)) {
                url = stringValue(result.get("url"));
            }
            String publicId = stringValue(result.get("public_id"));
            String format = stringValue(result.get("format"));
            Integer width = intValue(result.get("width"));
            Integer height = intValue(result.get("height"));
            long bytesStored = longValue(result.get("bytes"), sizeBytes);
            return new StoredMedia(
                    url,
                    publicId,
                    format != null ? format.toLowerCase(Locale.ROOT) : null,
                    contentType,
                    bytesStored,
                    width,
                    height
            );
        } catch (BusinessConflictException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Cloudinary upload failed", ex);
            throw new BusinessConflictException("No se pudo subir la imagen a Cloudinary");
        }
    }

    @Override
    public void delete(String publicId) {
        if (!StringUtils.hasText(publicId)) {
            throw new BusinessConflictException("publicId es obligatorio");
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (Exception ex) {
            log.error("Cloudinary delete failed", ex);
            throw new BusinessConflictException("No se pudo eliminar la imagen en Cloudinary");
        }
    }

    private static String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Integer intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    private static long longValue(Object value, long fallback) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return fallback;
    }
}
