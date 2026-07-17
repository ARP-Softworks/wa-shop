package uy.washop.inquiry.api.dto;

import java.time.Instant;
import java.util.UUID;
import uy.washop.inquiry.domain.InquirySource;
import uy.washop.inquiry.domain.InquiryStatus;

public record InquiryResponse(
        UUID id,
        String customerName,
        String phone,
        String email,
        String message,
        UUID productId,
        InquiryStatus status,
        InquirySource source,
        Instant createdAt,
        Instant updatedAt
) {
}
