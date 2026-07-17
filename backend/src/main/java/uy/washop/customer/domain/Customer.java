package uy.washop.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import uy.washop.shared.domain.BaseEntity;

@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @NotBlank
    @Size(max = 40)
    @Column(nullable = false, length = 40)
    private String phone;

    @NotBlank
    @Size(max = 40)
    @Column(name = "phone_normalized", nullable = false, length = 40, unique = true)
    private String phoneNormalized;

    @Size(max = 320)
    @Column(length = 320)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoneNormalized() {
        return phoneNormalized;
    }

    public void setPhoneNormalized(String phoneNormalized) {
        this.phoneNormalized = phoneNormalized;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
