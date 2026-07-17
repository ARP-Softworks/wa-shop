package uy.washop.inquiry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uy.washop.product.domain.Product;
import uy.washop.shared.domain.BaseEntity;

@Entity
@Table(name = "inquiries")
public class Inquiry extends BaseEntity {

    @NotBlank
    @Size(max = 200)
    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Size(max = 40)
    @Column(length = 40)
    private String phone;

    @Email
    @Size(max = 320)
    @Column(length = 320)
    private String email;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private InquiryStatus status = InquiryStatus.NEW;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private InquirySource source;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public InquiryStatus getStatus() {
        return status;
    }

    public void setStatus(InquiryStatus status) {
        this.status = status;
    }

    public InquirySource getSource() {
        return source;
    }

    public void setSource(InquirySource source) {
        this.source = source;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
}
