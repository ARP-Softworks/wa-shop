package uy.washop.media.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uy.washop.media.api.dto.MediaDeleteRequest;
import uy.washop.media.api.dto.MediaUploadResponse;
import uy.washop.media.application.MediaApplicationService;

@RestController
@RequestMapping("/api/admin/media")
public class AdminMediaController {

    private final MediaApplicationService mediaApplicationService;

    public AdminMediaController(MediaApplicationService mediaApplicationService) {
        this.mediaApplicationService = mediaApplicationService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaUploadResponse upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "uploads") String folder
    ) {
        return mediaApplicationService.upload(file, folder);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Valid @RequestBody MediaDeleteRequest request) {
        mediaApplicationService.deleteIfUnreferenced(request.publicId());
    }
}
