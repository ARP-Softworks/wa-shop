package uy.washop.media.application;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import uy.washop.config.AppProperties;
import uy.washop.media.domain.DetectedImageType;
import uy.washop.shared.exception.BusinessConflictException;

@Component
public class MediaFileValidator {

    private final long maxFileSizeBytes;
    private final Set<String> allowedContentTypes;

    public MediaFileValidator(AppProperties appProperties) {
        this.maxFileSizeBytes = appProperties.getMedia().getMaxFileSize();
        this.allowedContentTypes = Arrays.stream(appProperties.getMedia().getAllowedTypes().split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public ValidatedMediaFile validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessConflictException("El archivo de imagen es obligatorio");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new BusinessConflictException(
                    "La imagen supera el tamaño máximo permitido (" + maxFileSizeBytes + " bytes)"
            );
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new BusinessConflictException("No se pudo leer el archivo de imagen");
        }

        DetectedImageType detected = detectType(bytes)
                .orElseThrow(() -> new BusinessConflictException(
                        "Tipo de archivo no permitido. Solo JPEG, PNG o WebP"
                ));

        if (!allowedContentTypes.contains(detected.contentType())) {
            throw new BusinessConflictException(
                    "Tipo de archivo no permitido: " + detected.contentType()
            );
        }

        Integer width = null;
        Integer height = null;
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image != null) {
                width = image.getWidth();
                height = image.getHeight();
            }
        } catch (IOException ignored) {
            // Dimensions are best-effort; upload can continue without them.
        }

        String safeFilename = generateSafeFilename(detected);
        return new ValidatedMediaFile(bytes, detected, safeFilename, width, height);
    }

    public static java.util.Optional<DetectedImageType> detectType(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return java.util.Optional.empty();
        }
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return java.util.Optional.of(DetectedImageType.JPEG);
        }
        if ((bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47
                && bytes[4] == 0x0D
                && bytes[5] == 0x0A
                && bytes[6] == 0x1A
                && bytes[7] == 0x0A) {
            return java.util.Optional.of(DetectedImageType.PNG);
        }
        // RIFF....WEBP
        boolean riff = bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F';
        boolean webp = bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
        if (riff && webp) {
            return java.util.Optional.of(DetectedImageType.WEBP);
        }
        return java.util.Optional.empty();
    }

    public static String generateSafeFilename(DetectedImageType type) {
        return java.util.UUID.randomUUID().toString().replace("-", "") + "." + type.extension();
    }

    public record ValidatedMediaFile(
            byte[] bytes,
            DetectedImageType type,
            String safeFilename,
            Integer width,
            Integer height
    ) {
    }
}
