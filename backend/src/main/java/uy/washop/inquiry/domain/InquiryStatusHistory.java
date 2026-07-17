package uy.washop.inquiry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import uy.washop.shared.domain.BaseEntity;

@Entity
@Table(name = "inquiry_status_history")
public class InquiryStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 40)
    private InquiryStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 40)
    private InquiryStatus toStatus;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(columnDefinition = "TEXT")
    private String note;

    public Inquiry getInquiry() {
        return inquiry;
    }

    public void setInquiry(Inquiry inquiry) {
        this.inquiry = inquiry;
    }

    public InquiryStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(InquiryStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public InquiryStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(InquiryStatus toStatus) {
        this.toStatus = toStatus;
    }

    public UUID getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(UUID changedBy) {
        this.changedBy = changedBy;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
