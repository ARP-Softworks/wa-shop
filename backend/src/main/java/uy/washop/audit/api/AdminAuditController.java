package uy.washop.audit.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.audit.api.dto.AuditLogResponse;
import uy.washop.audit.application.AdminAuditQueryService;
import uy.washop.shared.api.PageResponse;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditController {

    private final AdminAuditQueryService adminAuditQueryService;

    public AdminAuditController(AdminAuditQueryService adminAuditQueryService) {
        this.adminAuditQueryService = adminAuditQueryService;
    }

    @GetMapping
    public PageResponse<AuditLogResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminAuditQueryService.list(page, size);
    }
}
