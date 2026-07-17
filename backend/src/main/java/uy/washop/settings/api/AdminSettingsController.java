package uy.washop.settings.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.settings.api.dto.SiteSettingsResponse;
import uy.washop.settings.api.dto.SiteSettingsWriteRequest;
import uy.washop.settings.application.AdminSettingsService;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingsController {

    private final AdminSettingsService adminSettingsService;

    public AdminSettingsController(AdminSettingsService adminSettingsService) {
        this.adminSettingsService = adminSettingsService;
    }

    @GetMapping
    public SiteSettingsResponse get() {
        return adminSettingsService.get();
    }

    @PutMapping
    public SiteSettingsResponse update(@Valid @RequestBody SiteSettingsWriteRequest request) {
        return adminSettingsService.update(request);
    }
}
