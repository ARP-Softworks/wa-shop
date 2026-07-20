package uy.washop.banner.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.banner.api.dto.HeroBannerResponse;
import uy.washop.banner.api.dto.HeroBannerWriteRequest;
import uy.washop.banner.application.AdminHeroBannerService;

@RestController
@RequestMapping("/api/admin/hero-banners")
public class AdminHeroBannerController {

    private final AdminHeroBannerService adminHeroBannerService;

    public AdminHeroBannerController(AdminHeroBannerService adminHeroBannerService) {
        this.adminHeroBannerService = adminHeroBannerService;
    }

    @GetMapping
    public List<HeroBannerResponse> list() {
        return adminHeroBannerService.listAll();
    }

    @GetMapping("/{id}")
    public HeroBannerResponse get(@PathVariable UUID id) {
        return adminHeroBannerService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HeroBannerResponse create(@Valid @RequestBody HeroBannerWriteRequest request) {
        return adminHeroBannerService.create(request);
    }

    @PutMapping("/{id}")
    public HeroBannerResponse update(@PathVariable UUID id, @Valid @RequestBody HeroBannerWriteRequest request) {
        return adminHeroBannerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        adminHeroBannerService.delete(id);
    }
}
