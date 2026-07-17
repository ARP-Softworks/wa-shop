package uy.washop.media.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uy.washop.media.domain.DetectedImageType;

class MediaStorageServiceContractTest {

    @Test
    void providerNamesAreStableForApiResponses() {
        assertThat("local").isEqualTo("local");
        assertThat("cloudinary").isEqualTo("cloudinary");
        assertThat(DetectedImageType.JPEG.contentType()).isEqualTo("image/jpeg");
        assertThat(DetectedImageType.PNG.extension()).isEqualTo("png");
        assertThat(DetectedImageType.WEBP.contentType()).isEqualTo("image/webp");
    }

    @Test
    void safeFilenamesNeverReuseClientNames() {
        String generated = MediaFileValidator.generateSafeFilename(DetectedImageType.JPEG);
        assertThat(generated).doesNotContain("..");
        assertThat(generated).doesNotContain("/");
        assertThat(generated).doesNotContain("\\");
        assertThat(generated).endsWith(".jpg");
    }
}
