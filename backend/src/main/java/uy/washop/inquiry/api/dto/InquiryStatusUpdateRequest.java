package uy.washop.inquiry.api.dto;

import jakarta.validation.constraints.NotNull;
import uy.washop.inquiry.domain.InquiryStatus;

public record InquiryStatusUpdateRequest(
        @NotNull InquiryStatus status,
        String note,
        String adminNotes
) {
}
