package uy.washop.inquiry.api.dto;

import java.time.Instant;
import java.util.UUID;
import uy.washop.inquiry.domain.InquiryStatus;

public record InquiryStatusHistoryResponse(
        UUID id,
        InquiryStatus fromStatus,
        InquiryStatus toStatus,
        UUID changedBy,
        String note,
        Instant createdAt
) {
}
