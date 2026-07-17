package uy.washop.inquiry.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uy.washop.inquiry.domain.Inquiry;
import uy.washop.inquiry.domain.InquiryStatus;

public interface InquiryRepository extends JpaRepository<Inquiry, UUID> {

    List<Inquiry> findByStatusOrderByCreatedAtDesc(InquiryStatus status);

    List<Inquiry> findByProductIdOrderByCreatedAtDesc(UUID productId);

    @EntityGraph(attributePaths = "product")
    @Override
    Page<Inquiry> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "product")
    Page<Inquiry> findByStatus(InquiryStatus status, Pageable pageable);

    long countByStatus(InquiryStatus status);
}
