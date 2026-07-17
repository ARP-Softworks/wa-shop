package uy.washop.inquiry.api.mapper;

import java.util.List;
import uy.washop.inquiry.api.dto.AdminInquiryDetailResponse;
import uy.washop.inquiry.api.dto.AdminInquirySummaryResponse;
import uy.washop.inquiry.api.dto.InquiryResponse;
import uy.washop.inquiry.api.dto.InquiryStatusHistoryResponse;
import uy.washop.inquiry.domain.Inquiry;
import uy.washop.inquiry.domain.InquiryStatusHistory;

public final class InquiryMapper {

    private InquiryMapper() {
    }

    public static InquiryResponse toResponse(Inquiry inquiry) {
        return new InquiryResponse(
                inquiry.getId(),
                inquiry.getCustomerName(),
                inquiry.getPhone(),
                inquiry.getEmail(),
                inquiry.getMessage(),
                inquiry.getProduct() != null ? inquiry.getProduct().getId() : null,
                inquiry.getStatus(),
                inquiry.getSource(),
                inquiry.getCreatedAt(),
                inquiry.getUpdatedAt()
        );
    }

    public static AdminInquirySummaryResponse toAdminSummary(Inquiry inquiry) {
        return new AdminInquirySummaryResponse(
                inquiry.getId(),
                inquiry.getCustomerName(),
                inquiry.getPhone(),
                inquiry.getEmail(),
                inquiry.getStatus(),
                inquiry.getSource(),
                inquiry.getProduct() != null ? inquiry.getProduct().getId() : null,
                inquiry.getProduct() != null ? inquiry.getProduct().getName() : null,
                inquiry.getCreatedAt(),
                inquiry.getUpdatedAt()
        );
    }

    public static AdminInquiryDetailResponse toAdminDetail(
            Inquiry inquiry,
            List<InquiryStatusHistory> history
    ) {
        return new AdminInquiryDetailResponse(
                inquiry.getId(),
                inquiry.getCustomerName(),
                inquiry.getPhone(),
                inquiry.getEmail(),
                inquiry.getMessage(),
                inquiry.getAdminNotes(),
                inquiry.getStatus(),
                inquiry.getSource(),
                inquiry.getProduct() != null ? inquiry.getProduct().getId() : null,
                inquiry.getProduct() != null ? inquiry.getProduct().getName() : null,
                inquiry.getProduct() != null ? inquiry.getProduct().getSlug() : null,
                inquiry.getCreatedAt(),
                inquiry.getUpdatedAt(),
                history.stream().map(InquiryMapper::toHistory).toList()
        );
    }

    public static InquiryStatusHistoryResponse toHistory(InquiryStatusHistory entry) {
        return new InquiryStatusHistoryResponse(
                entry.getId(),
                entry.getFromStatus(),
                entry.getToStatus(),
                entry.getChangedBy(),
                entry.getNote(),
                entry.getCreatedAt()
        );
    }
}
