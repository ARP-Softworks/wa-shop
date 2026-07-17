package uy.washop.media.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uy.washop.config.AppProperties;
import uy.washop.media.domain.StoredMedia;
import uy.washop.shared.exception.BusinessConflictException;

class LocalMediaStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalMediaStorageService storage;

    @BeforeEach
    void setUp() throws Exception {
        AppProperties properties = new AppProperties();
        properties.setPublicBaseUrl("http://localhost:8080");
        properties.getMedia().getLocal().setBaseDir(tempDir.toString());
        storage = new LocalMediaStorageService(properties);
    }

    @Test
    void storesAndDeletesLocalFilesWithSafePublicIds() throws Exception {
        byte[] payload = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        StoredMedia stored = storage.store(
                new ByteArrayInputStream(payload),
                payload.length,
                "image/png",
                "abc123.png",
                "products"
        );

        assertThat(storage.providerName()).isEqualTo("local");
        assertThat(stored.publicId()).startsWith("local/products/");
        assertThat(stored.url()).startsWith("http://localhost:8080/media/products/");
        assertThat(Files.exists(tempDir.resolve("products").resolve("abc123.png"))).isTrue();

        storage.delete(stored.publicId());
        assertThat(Files.exists(tempDir.resolve("products").resolve("abc123.png"))).isFalse();
    }

    @Test
    void rejectsPathTraversalOnDelete() {
        assertThatThrownBy(() -> storage.delete("local/../secret.txt"))
                .isInstanceOf(BusinessConflictException.class);
    }
}
