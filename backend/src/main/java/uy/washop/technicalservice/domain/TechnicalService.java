package uy.washop.technicalservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import uy.washop.shared.domain.BaseEntity;
import uy.washop.shared.domain.CurrencyCode;

@Entity
@Table(name = "technical_services")
public class TechnicalService extends BaseEntity {

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @NotBlank
    @Size(max = 220)
    @Column(nullable = false, length = 220, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @DecimalMin("0.00")
    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(length = 3)
    private CurrencyCode currency;

    @Size(max = 120)
    @Column(name = "estimated_time", length = 120)
    private String estimatedTime;

    @Column(nullable = false)
    private boolean active = true;

    @Size(max = 70)
    @Column(name = "seo_title", length = 70)
    private String seoTitle;

    @Size(max = 320)
    @Column(name = "meta_description", length = 320)
    private String metaDescription;

    @Column(nullable = false)
    private boolean indexable = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyCode currency) {
        this.currency = currency;
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSeoTitle() {
        return seoTitle;
    }

    public void setSeoTitle(String seoTitle) {
        this.seoTitle = seoTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public boolean isIndexable() {
        return indexable;
    }

    public void setIndexable(boolean indexable) {
        this.indexable = indexable;
    }
}
