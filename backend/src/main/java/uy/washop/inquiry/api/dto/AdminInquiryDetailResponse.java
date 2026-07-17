package uy.washop.inquiry.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import uy.washop.inquiry.domain.InquirySource;
import uy.washop.inquiry.domain.InquiryStatus;

public record AdminInquiryDetailResponse(
        UUID id,
        String customerName,
        String phone,
        String email,
        String message,
        String adminNotes,
        InquiryStatus status,
        InquirySource source,
        UUID productId,
        String productName,
        String productSlug,
        Instant createdAt,
        Instant updatedAt,
        List<InquiryStatusHistoryResponse> statusHistory
) {
}
