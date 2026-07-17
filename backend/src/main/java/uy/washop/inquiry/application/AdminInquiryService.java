package uy.washop.inquiry.application;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uy.washop.audit.application.AuditService;
import uy.washop.audit.domain.AuditAction;
import uy.washop.inquiry.api.dto.AdminInquiryDetailResponse;
import uy.washop.inquiry.api.dto.AdminInquirySummaryResponse;
import uy.washop.inquiry.api.dto.InquiryStatusUpdateRequest;
import uy.washop.inquiry.api.mapper.InquiryMapper;
import uy.washop.inquiry.domain.Inquiry;
import uy.washop.inquiry.domain.InquiryStatus;
import uy.washop.inquiry.domain.InquiryStatusHistory;
import uy.washop.inquiry.infrastructure.InquiryRepository;
import uy.washop.inquiry.infrastructure.InquiryStatusHistoryRepository;
import uy.washop.shared.api.PageResponse;
import uy.washop.shared.exception.ResourceNotFoundException;

@Service
public class AdminInquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryStatusHistoryRepository historyRepository;
    private final AuditService auditService;

    public AdminInquiryService(
            InquiryRepository inquiryRepository,
            InquiryStatusHistoryRepository historyRepository,
            AuditService auditService
    ) {
        this.inquiryRepository = inquiryRepository;
        this.historyRepository = historyRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminInquirySummaryResponse> search(InquiryStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 48),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<Inquiry> result = status == null
                ? inquiryRepository.findAll(pageable)
                : inquiryRepository.findByStatus(status, pageable);
        return new PageResponse<>(
                result.getContent().stream().map(InquiryMapper::toAdminSummary).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public AdminInquiryDetailResponse getById(UUID id) {
        Inquiry inquiry = require(id);
        List<InquiryStatusHistory> history = historyRepository.findByInquiryIdOrderByCreatedAtDesc(id);
        return InquiryMapper.toAdminDetail(inquiry, history);
    }

    @Transactional
    public AdminInquiryDetailResponse updateStatus(UUID id, InquiryStatusUpdateRequest request) {
        Inquiry inquiry = require(id);
        InquiryStatus previous = inquiry.getStatus();
        if (request.status() != previous) {
            InquiryStatusHistory history = new InquiryStatusHistory();
            history.setInquiry(inquiry);
            history.setFromStatus(previous);
            history.setToStatus(request.status());
            history.setChangedBy(auditService.currentUserId());
            history.setNote(StringUtils.hasText(request.note()) ? request.note().trim() : null);
            historyRepository.save(history);
            inquiry.setStatus(request.status());
        }
        if (request.adminNotes() != null) {
            inquiry.setAdminNotes(StringUtils.hasText(request.adminNotes()) ? request.adminNotes().trim() : null);
        }
        inquiryRepository.save(inquiry);
        auditService.record(
                AuditAction.UPDATE,
                "Inquiry",
                id,
                "Consulta actualizada a " + inquiry.getStatus()
        );
        return InquiryMapper.toAdminDetail(
                inquiry,
                historyRepository.findByInquiryIdOrderByCreatedAtDesc(id)
        );
    }

    private Inquiry require(UUID id) {
        return inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta no encontrada"));
    }
}
