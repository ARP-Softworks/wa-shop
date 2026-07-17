package uy.washop.inquiry.api;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uy.washop.inquiry.api.dto.AdminInquiryDetailResponse;
import uy.washop.inquiry.api.dto.AdminInquirySummaryResponse;
import uy.washop.inquiry.api.dto.InquiryStatusUpdateRequest;
import uy.washop.inquiry.application.AdminInquiryService;
import uy.washop.inquiry.domain.InquiryStatus;
import uy.washop.shared.api.PageResponse;

@RestController
@RequestMapping("/api/admin/inquiries")
public class AdminInquiryController {

    private final AdminInquiryService adminInquiryService;

    public AdminInquiryController(AdminInquiryService adminInquiryService) {
        this.adminInquiryService = adminInquiryService;
    }

    @GetMapping
    public PageResponse<AdminInquirySummaryResponse> search(
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return adminInquiryService.search(status, page, size);
    }

    @GetMapping("/{id}")
    public AdminInquiryDetailResponse get(@PathVariable UUID id) {
        return adminInquiryService.getById(id);
    }

    @PutMapping("/{id}/status")
    public AdminInquiryDetailResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody InquiryStatusUpdateRequest request
    ) {
        return adminInquiryService.updateStatus(id, request);
    }
}
