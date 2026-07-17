package uy.washop.inquiry.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uy.washop.inquiry.domain.InquiryStatusHistory;

public interface InquiryStatusHistoryRepository extends JpaRepository<InquiryStatusHistory, UUID> {

    List<InquiryStatusHistory> findByInquiryIdOrderByCreatedAtDesc(UUID inquiryId);
}
