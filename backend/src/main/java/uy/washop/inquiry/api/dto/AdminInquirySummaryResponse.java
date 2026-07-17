package uy.washop.inquiry.api.dto;

import java.time.Instant;
import java.util.UUID;
import uy.washop.inquiry.domain.InquirySource;
import uy.washop.inquiry.domain.InquiryStatus;

public record AdminInquirySummaryResponse(
        UUID id,
        String customerName,
        String phone,
        String email,
        InquiryStatus status,
        InquirySource source,
        UUID productId,
        String productName,
        Instant createdAt,
        Instant updatedAt
) {
}
