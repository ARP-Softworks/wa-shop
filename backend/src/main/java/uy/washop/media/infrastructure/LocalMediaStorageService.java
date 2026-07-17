package uy.washop.media.infrastructure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
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
@ConditionalOnProperty(name = "app.media.provider", havingValue = "local")
public class LocalMediaStorageService implements MediaStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalMediaStorageService.class);

    private final Path baseDir;
    private final String publicBaseUrl;

    public LocalMediaStorageService(AppProperties appProperties) throws IOException {
        this.baseDir = Path.of(appProperties.getMedia().getLocal().getBaseDir()).toAbsolutePath().normalize();
        this.publicBaseUrl = trimTrailingSlash(appProperties.getPublicBaseUrl());
        Files.createDirectories(this.baseDir);
        log.info("Local media storage enabled at {}", this.baseDir);
    }

    @Override
    public String providerName() {
        return "local";
    }

    @Override
    public StoredMedia store(
            InputStream content,
            long sizeBytes,
            String contentType,
            String safeFilename,
            String folder
    ) {
        try {
            Path folderPath = resolveSafePath(folder);
            Files.createDirectories(folderPath);
            Path target = folderPath.resolve(safeFilename).normalize();
            if (!target.startsWith(baseDir)) {
                throw new BusinessConflictException("Ruta de almacenamiento inválida");
            }
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
            String relative = baseDir.relativize(target).toString().replace('\\', '/');
            String publicId = "local/" + relative;
            String url = publicBaseUrl + "/media/" + relative;
            String format = extensionOf(safeFilename);
            return new StoredMedia(url, publicId, format, contentType, sizeBytes, null, null);
        } catch (BusinessConflictException ex) {
            throw ex;
        } catch (IOException ex) {
            log.error("Local media store failed", ex);
            throw new BusinessConflictException("No se pudo guardar la imagen en almacenamiento local");
        }
    }

    @Override
    public void delete(String publicId) {
        if (!StringUtils.hasText(publicId) || !publicId.startsWith("local/")) {
            throw new BusinessConflictException("publicId local inválido");
        }
        String relative = publicId.substring("local/".length());
        try {
            Path target = resolveSafePath(relative);
            if (!target.startsWith(baseDir)) {
                throw new BusinessConflictException("Ruta de almacenamiento inválida");
            }
            Files.deleteIfExists(target);
        } catch (BusinessConflictException ex) {
            throw ex;
        } catch (IOException ex) {
            log.error("Local media delete failed", ex);
            throw new BusinessConflictException("No se pudo eliminar la imagen local");
        }
    }

    private Path resolveSafePath(String relative) {
        Path resolved = baseDir.resolve(relative).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new BusinessConflictException("Ruta de almacenamiento inválida");
        }
        return resolved;
    }

    private static String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return null;
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
