package uy.washop.media.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uy.washop.config.AppProperties;
import uy.washop.media.domain.DetectedImageType;
import uy.washop.shared.exception.BusinessConflictException;

class MediaFileValidatorTest {

    private MediaFileValidator validator;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties();
        properties.getMedia().setMaxFileSize(1024);
        properties.getMedia().setAllowedTypes("image/jpeg,image/png,image/webp");
        validator = new MediaFileValidator(properties);
    }

    @Test
    void detectsJpegPngAndWebpFromMagicBytes() {
        assertThat(MediaFileValidator.detectType(jpegBytes())).contains(DetectedImageType.JPEG);
        assertThat(MediaFileValidator.detectType(pngBytes())).contains(DetectedImageType.PNG);
        assertThat(MediaFileValidator.detectType(webpBytes())).contains(DetectedImageType.WEBP);
        assertThat(MediaFileValidator.detectType("not-an-image".getBytes(StandardCharsets.UTF_8))).isEmpty();
    }

    @Test
    void rejectsDisallowedOrOversizedFiles() {
        AppProperties properties = new AppProperties();
        properties.getMedia().setMaxFileSize(8);
        properties.getMedia().setAllowedTypes("image/png");
        MediaFileValidator strict = new MediaFileValidator(properties);

        assertThatThrownBy(() -> strict.validate(new InMemoryMultipartFile("x.bin", jpegBytes())))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("tamaño máximo");

        assertThatThrownBy(() -> validator.validate(new InMemoryMultipartFile("x.bin", "hello-world".getBytes())))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("no permitido");
    }

    @Test
    void acceptsValidPngAndGeneratesSafeFilename() {
        var validated = validator.validate(new InMemoryMultipartFile("photo.PNG", pngBytes()));
        assertThat(validated.type()).isEqualTo(DetectedImageType.PNG);
        assertThat(validated.safeFilename()).matches("^[a-f0-9]{32}\\.png$");
        assertThat(validated.safeFilename()).doesNotContain("..");
    }

    private static byte[] jpegBytes() {
        byte[] bytes = new byte[64];
        bytes[0] = (byte) 0xFF;
        bytes[1] = (byte) 0xD8;
        bytes[2] = (byte) 0xFF;
        return bytes;
    }

    private static byte[] pngBytes() {
        return new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
    }

    private static byte[] webpBytes() {
        byte[] bytes = new byte[16];
        bytes[0] = 'R';
        bytes[1] = 'I';
        bytes[2] = 'F';
        bytes[3] = 'F';
        bytes[8] = 'W';
        bytes[9] = 'E';
        bytes[10] = 'B';
        bytes[11] = 'P';
        return bytes;
    }

    private static final class InMemoryMultipartFile implements org.springframework.web.multipart.MultipartFile {
        private final String name;
        private final byte[] content;

        private InMemoryMultipartFile(String name, byte[] content) {
            this.name = name;
            this.content = content;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            return "application/octet-stream";
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public java.io.InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws java.io.IOException {
            java.nio.file.Files.write(dest.toPath(), content);
        }
    }
}
